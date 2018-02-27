package br.com.dsx.vendamais.sync;

import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.w3c.dom.Document;

import java.util.List;

import javax.xml.xpath.XPathConstants;

import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.SankhyaException;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.ItemPedidoDao;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.dao.ProdutoDao;
import br.com.dsx.vendamais.dao.TipoOperacaoDao;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.ItemPedido;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.domain.Produto;
import br.com.dsx.vendamais.domain.TipoOperacao;


public class SyncTaskPedido extends SyncTask {



    public SyncTaskPedido(ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        String dtNeg, emailUsuario, xmlAsString;
        try {
            PedidoDao pedidoDao = daoSession.getPedidoDao();

            QueryBuilder<Pedido> queryBuilderPedido = pedidoDao.queryBuilder();
            //Filtra pedidos com número único zerado. Se está zerado é porque ainda não
            //foi sincronizado
            queryBuilderPedido.where(PedidoDao.Properties.NumeroUnico.eq(0));

            Query<Pedido> queryPedido = queryBuilderPedido.build();
            List<Pedido> pedidos = queryPedido.list();

            if (pedidos.isEmpty()){
                publishProgress(0, 1);
                publishProgress(1);
                messages.add(new Message(Message.Type.SUCESS, "Pedidos sincronizados."));
                return messages;
            }

            //RECUPERA O PERFIL DO USUÁRIO LOGADO
            List<Perfil> list =  daoSession.getPerfilDao().loadAll();
            if (!list.isEmpty()) {
                emailUsuario = list.get(0).getEmail();
            }else {
                String msg = "Não foi possível obter o e-mail do usuário logado.";
                messages.add(new Message(Message.Type.ERROR, msg));
                return messages;
            }

            String sql = " SELECT"
                    + " CODVEND," //0
                    + " CODCENCUSPAD," //1
                    + " (SELECT INTEIRO FROM TSIPAR P1 WHERE P1.CHAVE = 'AD_CODNATPAD') AS CODNAT " //2
                    + " FROM TGFVEN"
                    + " WHERE EMAIL = '"
                    + emailUsuario
                    + "'";

            int codVend = 0;
            int codCenCus = 0;
            int codNat = 0;

            JSONArray linha = SankhyaUtil.execSelect(serviceInvoker, sql);
            if (linha.length()>0) {
                linha = linha.getJSONArray(0);

                codVend = linha.getInt(0);
                codCenCus = linha.getInt(1);
                codNat = linha.getInt(2);
            }
            //TODO: no caso da karine@ceitel.com.br, deveria lançar um erro que não encontrou vendedor

            for (Pedido p : pedidos) {
                dtNeg = Util.dateToString("dd/MM/yyyy", p.getDataInclusao());

                String obs = p.getObservacao();
                if (obs == null) {
                    obs = "";
                }

                xmlAsString = "<nota><cabecalho>"
                        + "<NUNOTA />"
                        + "<CODPARC>" + p.getIdCliente() + "</CODPARC>"
                        + "<DTNEG>" + dtNeg + "</DTNEG>"
                        + "<CODEMP>" + p.getIdEmpresa() + "</CODEMP>"
                        + "<CODCENCUS>" + codCenCus + "</CODCENCUS>"
                        + "<CODNAT>" + codNat + "</CODNAT>"
                        + "<CODVEND>" + codVend + "</CODVEND>"
                        + "<CODTIPOPER>" + p.getIdTipoOperacao() + "</CODTIPOPER>"
                        + "<CODTIPVENDA>" + p.getIdFormaPagamento() + "</CODTIPVENDA>"
                        + "<NUMNOTA>0</NUMNOTA>"
                        + "<VLRNOTA>" + p.getValorTotal() + "</VLRNOTA>"
                        + "<AD_OBSAD>" + obs + "</AD_OBSAD>"
                        + "<SERIENOTA>1</SERIENOTA>"
                        + "<CIF_FOB>F</CIF_FOB>"
                        + "<AD_VENDAMAIS>S</AD_VENDAMAIS>"
                        + "<TIPMOV>P</TIPMOV>"
                        + "</cabecalho>";

                xmlAsString += getXmlItens(p);
                xmlAsString += "</nota>";

                try {
                    Document docRet = serviceInvoker.call("CACSP.incluirNota", "mgecom", xmlAsString);

                    String nunota = (String)SWServiceInvoker.xpath(docRet, "/serviceResponse/responseBody/pk/NUNOTA/text()", XPathConstants.STRING);

                    //Informa o número único do pedido gerado no Sankhya
                    p.setNumeroUnico(Integer.parseInt(nunota));

                    //Salva alteração feita no pedido
                    pedidoDao.save(p);

                    try {
                        //Tenta confirmar o pedido no sistema Sankhya
                        confirmarPedido(p.getNumeroUnico());
                    } catch (SankhyaException e1){
                        //e1.printStackTrace();
                        Message msg = new Message(Message.Type.ERROR,
                                p.toValidationString()+"\nFoi enviado mas não foi possível confirmar.\n"
                                + e1.toString()
                                );
                        messages.add(msg);
                        String erromsg = "Erro ao confirmar o pedido nro unico "
                                    + p.getNumeroUnico()
                                    + ".\n"
                                    + e1.toString();
                        daoSession.getErroDao().insert(new Erro(erromsg));
                    }



                } catch (SankhyaException e1) {
                    p.setNumeroUnico(0);

                    e1.printStackTrace();
                    Message msg = new Message(Message.Type.BUSINESS,
                            p.toValidationString()+"\n"+ Util.cleanMessage(e1.getMessage()));
                    msg.setEntityId(p.getId());
                    msg.setEntityName(Pedido.class.getSimpleName());
                    messages.add(msg);
                    daoSession.getErroDao().insert(new Erro(e1.toString()));

                } catch (Exception e1) {
                    p.setNumeroUnico(0);

                    e1.printStackTrace();
                    Message msg = new Message(Message.Type.ERROR,
                            p.toValidationString()+"\n"+ e1.toString());
                    messages.add(msg);
                    daoSession.getErroDao().insert(new Erro(e1.toString()));
                }
            } //for


        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        if (messages.isEmpty()) {
            messages.add(new Message(Message.Type.SUCESS, "Pedidos sincronizados."));
        }
        return messages;
    }

    private void confirmarPedido(int nroUnico) throws Exception{
        String xmlAsString = "<nota confirmacaoCentralNota=\"true\" ehPedidoWeb=\"false\" atualizaPrecoItemPedCompra=\"false\" ownerServiceCall=\"CentralNotas6248\">" +
                                "<NUNOTA>" + nroUnico + "</NUNOTA>" +
                                "<txProperties>" +
                                "<prop name=\"br.com.utiliza.dtneg.servidor\" value=\"false\"/>" +
                                "</txProperties>" +
                             "</nota>";

        serviceInvoker.call("CACSP.confirmarNota", "mgecom", xmlAsString);
    }

    private String getXmlItens(Pedido pedido){
        ItemPedidoDao itemDao  = daoSession.getItemPedidoDao();
        ProdutoDao produtoDao  = daoSession.getProdutoDao();
        TipoOperacaoDao topDao = daoSession.getTipoOperacaoDao();

        QueryBuilder<ItemPedido> queryBuilderItemPedido = itemDao.queryBuilder();

        queryBuilderItemPedido.where(ItemPedidoDao.Properties.IdPedido.eq( pedido.getId() ));

        Query<ItemPedido> queryItemPedido = queryBuilderItemPedido.build();
        List<ItemPedido> itens = queryItemPedido.list();

        double vlrTotal, vlrUnitLiq;

        TipoOperacao top = topDao.load( pedido.getIdTipoOperacao() );
        int atualEstoque = "S".equals(top.getReserva()) ? 1 : 0;

        String xmlAsString = "<itens>";
        Produto prod;

        for (ItemPedido ite : itens) {
            //System.out.println(ite);
            Double qtd = ite.getQuantidade();
            if (qtd!=null && qtd>0) {
                vlrUnitLiq = ite.getValorTaxado() - ite.getDesconto()/ite.getQuantidade();
            } else {
                vlrUnitLiq = ite.getValorTaxado() - ite.getDesconto();
            }
            vlrTotal = vlrUnitLiq * ite.getQuantidade();
            //vlrDesc  = ite.getDesconto();
            prod = produtoDao.load(ite.getIdProduto());

            xmlAsString += "<item>"
                    + "<NUNOTA />"
                    + "<SEQUENCIA />"
                    + "<CODPROD>" + ite.getIdProduto() + "</CODPROD>"
                    + "<QTDNEG>" + ite.getQuantidade() + "</QTDNEG>"
                    + "<VLRUNIT>" + vlrUnitLiq + "</VLRUNIT>"
                    + "<VLRTOT>" + vlrTotal + "</VLRTOT>"
                    + "<CODVOL>" + prod.getCodigoVolume() + "</CODVOL>"
                    + "<CODLOCALORIG>" + pedido.getIdLocalEstoque() + "</CODLOCALORIG>"
                    + "<RESERVA>" + top.getReserva() + "</RESERVA>"
                    + "<ATUALESTOQUE>" + atualEstoque + "</ATUALESTOQUE>"
                    //+ "<VLRDESC>" + ite.getDesconto() + "</VLRDESC>"
                    + "</item>";
        }

        xmlAsString += "</itens>";

        return xmlAsString;
    }

}
