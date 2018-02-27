package br.com.dsx.vendamais.sync;

import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import br.com.dsx.vendamais.common.JSONUtil;
import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.SankhyaException;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Erro;


public class SyncTaskCliente extends SyncTask {

    public interface Tipo {
        int ENVIAR = 1;
        int BAIXAR = 2;
    }

    private int tipo;

    public SyncTaskCliente(int tipo, ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
        this.tipo = tipo;
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        if (params != null && params.length > 0) {
            ultimaSincronizacao = params[0];
        }

        try {

            // testar a conexão antes de qualquer outra coisa.
            serviceInvoker.doLogin();

        } catch (java.net.ConnectException e1) {
            e1.printStackTrace();
            StringBuilder erro = new StringBuilder();
            erro.append("Problemas com a conexão com o servidor. Verifique sua conexão com a internet.\n\n");
            erro.append("Informação Técnica: "+e1.toString());
            messages.add(new Message(Message.Type.ERROR, erro.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }


        try {
            //Faz upload dos clientes cadastrados no app
            if (tipo == Tipo.ENVIAR) {
                enviarTodosClientes();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.BUSINESS, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }

        try {
            //Baixa dos clientes cadastrados no sistema Sankhya
            if (tipo == Tipo.BAIXAR) {
                baixarClientes();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }

        if (messages.isEmpty()) {
            messages.add(new Message(Message.Type.SUCESS, "Clientes sincronizados."));
        }
        return messages;
    }

    public void baixarClientes() throws Exception {

        //Pega a quantidade de clientes que vamos sincronizar.
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COUNT(1)")
                .append(" FROM TGFPAR PAR")
                .append(" WHERE PAR.CLIENTE = 'S'")
                .append(" AND PAR.CODPARC > 0")
                .append(" AND PAR.ATIVO   = 'S'");
        if (Util.isNotBlank(ultimaSincronizacao)) {
            //Adicionei TRUNC na data para remover a HORA. Estava ocorrendo alguns erros em que
            //o parceiro era alteradoc/cadastrado no Sankhya, mas não era baixado no app devido
            //a problema com hora
            sql.append(" AND PAR.DTALTER >= TRUNC(TO_DATE('")
                    .append(ultimaSincronizacao)
                    .append("', 'yyyy/MM/dd HH24:MI'))");
        }

        JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
        JSONArray linha = linhas.getJSONArray(0);
        int qtdParceiros = linha.getInt(0);
        if (qtdParceiros == 0) {
            publishProgress(0, 1);
            publishProgress(1);
            return;
        }

        publishProgress(0, qtdParceiros);

        //Armazena o código do último cliente sincronizado. Será utilizado no SELECT
        int ultimoClienteId = 0;

        //Cada consulta jSON retorna até 5000 parceiros
        for (int k = 0; qtdParceiros > 0 && k <= Math.ceil(qtdParceiros / 5000); k++) {
            sql = new StringBuilder();
            sql.append(" SELECT ")
                    .append(" PAR.CODPARC,")//0
                    .append(" PAR.TIPPESSOA,")//1
                    .append(" PAR.NOMEPARC,")//2
                    .append(" PAR.RAZAOSOCIAL,")//3
                    .append(" PAR.CGC_CPF,")//4
                    .append(" PAR.CEP,")//5
                    .append(" NVL(PAR.FAX, PAR.TELEFONE) AS TELEFONE,")//6
                    .append(" RTRIM(CID.NOMECID) AS CIDADE,")//7
                    .append(" UFS.UF,")//8
                    .append(" RTRIM(BAI.NOMEBAI) AS BAIRRO,")//9
                    .append(" END.TIPO,")//10
                    .append(" END.NOMEEND,")//11
                    .append(" PAR.NUMEND,")//12
                    .append(" PAR.COMPLEMENTO,")//13
                    .append(" UFS.CODUF,")//14
                    .append(" PAR.EMAIL,")//15
                    .append(" PAR.IDENTINSCESTAD AS IE")//16 - IE - Inscrição Estadual
                    .append(" FROM TGFPAR PAR")
                    .append(" INNER JOIN TSICID CID ON PAR.CODCID = CID.CODCID")
                    .append(" INNER JOIN TSIUFS UFS ON CID.UF = UFS.CODUF")
                    .append(" LEFT OUTER JOIN TSIBAI BAI ON PAR.CODBAI = BAI.CODBAI")
                    .append(" LEFT OUTER JOIN TSIEND END ON PAR.CODEND = END.CODEND")
                    .append(" WHERE PAR.CLIENTE = 'S'")
                    .append(" AND PAR.ATIVO   = 'S'")
                    .append(" AND PAR.CODPARC > ").append(ultimoClienteId);
            if (Util.isNotBlank(ultimaSincronizacao)) {
                sql.append(" AND PAR.DTALTER >= TRUNC(TO_DATE('")
                        .append(ultimaSincronizacao)
                        .append("', 'YYYY/MM/DD HH24:MI'))");
            }
            sql.append(" ORDER BY PAR.CODPARC");

            linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
            if (linhas != null && linhas.length() > 0) {
                ultimoClienteId = dumpJsonToCliente(linhas);
            } else {
                break;
            }
        }
    }

    /**
     * Salva a lista de clientes no banco de dados.
     *
     * @param lista listagem com os clintes no formato JSONArray que é o resultado da consulta no SankhyaW
     * @return Retorna o código do último cliente inserido no BD
     */
    private int dumpJsonToCliente(JSONArray lista) {
        try {
            JSONArray linha = null;
            List<Cliente> clientes = new ArrayList<>();
            for (int i = 0; i < lista.length(); i++) {
                linha = lista.getJSONArray(i);

                //Atualiza a progressBar na interface do usuário
                progressoGeral++;
                publishProgress(progressoGeral);

                Cliente cliente = new Cliente();
                cliente.setId(linha.getLong(0));
                cliente.setCodParcSk(linha.getInt(0));
                if (linha.getString(1).equals("J")) {
                    cliente.setTipo(0);
                } else {
                    cliente.setTipo(1);
                }
                cliente.setNomeFantasia(JSONUtil.getString(linha, 2));
                cliente.setNomeRazaoSocial(JSONUtil.getString(linha, 3).trim());
                cliente.setCpfCnpj(JSONUtil.getString(linha, 4));
                cliente.setCep(JSONUtil.getString(linha, 5));
                String celular = JSONUtil.getString(linha, 6);
                celular = celular.replaceAll("\\D+", Util.STRING_EMPTY);
                if (celular.startsWith("0")) {
                    celular = celular.substring(1);
                }
                cliente.setCelular(celular);
                cliente.setCidade(JSONUtil.getString(linha, 7));
                cliente.setUf(JSONUtil.getString(linha, 8));
                cliente.setLogradouroAutocomplete(true);
                cliente.setBairro(JSONUtil.getString(linha, 9));
                cliente.setTipoLogradouro(JSONUtil.getString(linha, 10));
                cliente.setLogradouro(JSONUtil.getString(linha, 11));
                cliente.setNumero(JSONUtil.getString(linha, 12));
                cliente.setComplemento(JSONUtil.getString(linha, 13));
                int codUf = Integer.parseInt(JSONUtil.getString(linha, 14));
                cliente.setCodUf(codUf);
                cliente.setEmail(JSONUtil.getString(linha, 15));
                cliente.setInscricaoEstadual( JSONUtil.getString(linha, 16) );

                clientes.add(cliente);
            }
            daoSession.getClienteDao().insertOrReplaceInTx(clientes);
            if (linha != null) {
                return linha.getInt(0);
            }
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }
        return 0;
    }


    private void enviarTodosClientes() throws Exception {
        ClienteDao clienteDao = daoSession.getClienteDao();
        QueryBuilder<Cliente> queryBuilder = clienteDao.queryBuilder();
        //Filtra apenas parceiros que foram cadastrados e estão com código zero.
        queryBuilder.where(ClienteDao.Properties.CodParcSk.eq(0));

        Query<Cliente> query = queryBuilder.build();
        List<Cliente> clientes = query.list();

        enviarClientes(clientes);
    }

    private void enviarClientes(List<Cliente> clientes) throws Exception {
        String xmlAsString = "";
        String tipoPessoa, logradouro, tipoLogradouro, nomeBairro, cidade, nomeFantasia, complemento;
        ClienteDao clienteDao = daoSession.getClienteDao();
        Database db = daoSession.getDatabase();
        String updatePedidosId = "update "+PedidoDao.TABLENAME
                + " set "+PedidoDao.Properties.IdCliente.columnName + "=?"
                +" where " + PedidoDao.Properties.IdCliente.columnName + "=?";

        if (clientes.isEmpty()) {
            return;
        }

        //Busca o perfil padrão de clientes cadastrados pelo app
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT INTEIRO")
                .append(" FROM TSIPAR")
                .append(" WHERE CHAVE = 'AD_CODPERFILAPP'");

        JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
        JSONArray linha = linhas.getJSONArray(0);
        int codTipParc = linha.getInt(0);

        for (Cliente c1 : clientes) {
            //System.out.println(c1);
            try {
                cidade = c1.getCidade();

                int uf = c1.getCodUf();
                int codcid = consultarCidade(cidade, uf);

                //Obtem código do endereço
                logradouro = c1.getLogradouro();
                tipoLogradouro = c1.getTipoLogradouro();
                int codend = consultarLogradouro(logradouro, tipoLogradouro);

                //Obtem código do bairro
                nomeBairro = c1.getBairro();
                int codBai = consultarBairro(nomeBairro);

                if (c1.getTipo() == 0)//PJ
                    tipoPessoa = "J";
                else
                    tipoPessoa = "F";

                nomeFantasia = c1.getNomeFantasia();

                if (nomeFantasia == null || nomeFantasia.isEmpty())
                    nomeFantasia = c1.getNomeRazaoSocial();


                complemento = c1.getComplemento() == null ? "" : c1.getComplemento();

                xmlAsString = "<entity name='Parceiro'>"
                        + "<campo nome='CODPARC'></campo>"
                        + "<campo nome='NOMEPARC'>" + Util.toString(nomeFantasia) + "</campo>"
                        + "<campo nome='RAZAOSOCIAL'>" + Util.toString(c1.getNomeRazaoSocial()) + "</campo>"
                        + "<campo nome='TIPPESSOA'>" + tipoPessoa + "</campo>"
                        + "<campo nome='CODEND'>" + codend + "</campo>"
                        + "<campo nome='CODBAI'>" + codBai + "</campo>"
                        + "<campo nome='CODCID'>" + codcid + "</campo>"
                        + "<campo nome='CGC_CPF'>" + Util.toString(c1.getCpfCnpj()) + "</campo>"
                        + "<campo nome='CEP'>" + Util.toString(c1.getCep()) + "</campo>"
                        + "<campo nome='NUMEND'>" + c1.getNumero() + "</campo>"
                        + "<campo nome='CLIENTE'>S</campo>"
                        + "<campo nome='FORNECEDOR'>N</campo>"
                        + "<campo nome='ATIVO'>S</campo>"
                        + "<campo nome='EMAIL'>" + Util.toString(c1.getEmail()) + "</campo>"
                        + "<campo nome='FAX'>" + Util.toString(c1.getCelular()) + "</campo>"
                        + "<campo nome='COMPLEMENTO'>" + Util.toString(complemento) + "</campo>"
                        + "<campo nome='CLASSIFICMS'>C</campo>"
                        + "<campo nome='BLOQUEAR'>S</campo>"
                        + "<campo nome='CODTIPPARC'>" + codTipParc + "</campo>"
                        + "</entity>";

                Document docRet = serviceInvoker.call("crud.save", "mge", xmlAsString);
                //printDocument(docRet);

                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                XPathExpression expr = xpath.compile("/serviceResponse/responseBody/entidades/entidade/CODPARC");
                String codparc = (String) expr.evaluate(docRet, XPathConstants.STRING);
                //Atualiza o código do cliente no BD do aplicativo como ele foi cadastrado com um código sequencial
                //E vamos atualizar com o código do Sankhya para ficar igual.
                Long id_old = c1.getId();

                //Apaga o cliente do banco de dados para que possa inseri-lo novamente com o novo
                //código gerado no Sankhya
                clienteDao.delete(c1);

                //Troca o código do cliente para inseri-lo novamente
                c1.setId(Long.parseLong(codparc));
                c1.setCodParcSk(Integer.parseInt(codparc));
                //Salva no BD já com novo código.
                long id = clienteDao.insert(c1);

                try {
                    db.beginTransaction();
                    db.execSQL(updatePedidosId, new Object[] {id, id_old});
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                daoSession.clear();

            } catch (SankhyaException e) {
                Message msg = new Message(Message.Type.BUSINESS, c1.toValidationString()+"\n"+e.getMessage());
                msg.setEntityId(c1.getId());
                msg.setEntityName(Cliente.class.getSimpleName());
                messages.add(msg);
                daoSession.getErroDao().insert(new Erro(e.toString()));
            } catch (Exception e) {
                Message msg = new Message(Message.Type.ERROR, c1.toValidationString()+"\n"+e.toString());
                messages.add(msg);
                daoSession.getErroDao().insert(new Erro(e.toString()));
            }
        } //for clientes


    }

    private int consultarLogradouro(String logradouro, String tipoLogradouro) throws Exception {
        if (logradouro == null)
            throw new SankhyaException("Nome do logradouro ou tipo não preenchido.");

        String nomeEnd = logradouro.toUpperCase();
        String tipo = (tipoLogradouro!=null)?tipoLogradouro.toUpperCase():Util.STRING_EMPTY;

        StringBuilder xml = new StringBuilder("<entity name='Endereco' rowsLimit='1' getPresentations='false'>");
        xml.append("<criterio nome='NOMEEND' valor='");
        xml.append(nomeEnd);
        xml.append("' />");

        if (!"".equals(tipo)){
            xml.append("<criterio nome='TIPO' valor='");
            xml.append(tipo);
            xml.append("' />");
        }

        xml.append("<fields>");
        xml.append("<field name='CODEND'/>");
        xml.append("</fields>");
        xml.append("</entity>");

        try {
            Document docRet = serviceInvoker.call("crud.find", "mge", xml.toString());
            //printDocument(docRet);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile("/serviceResponse/responseBody/entidades/entidade/CODEND");
            String codend = (String) expr.evaluate(docRet, XPathConstants.STRING);

            if ("".equals(codend)) {

                //XML para inserir uma nova cidade
                xml = new StringBuilder("<entity name='Endereco'>");
                xml.append("<campo nome='NOMEEND'>");
                xml.append(nomeEnd);
                xml.append("</campo>");
                xml.append("<campo nome='TIPO'>");
                xml.append(tipo);
                xml.append("</campo>");
                xml.append("</entity>");

                docRet = serviceInvoker.call("crud.save", "mge", xml.toString());

                codend = (String) expr.evaluate(docRet, XPathConstants.STRING);
                //System.out.println("Endereço cadastrado com sucesso! Código : " + codend);
            }
            /*else {
                System.out.println("Endereço existente: " + codend);
            }*/

            return Integer.parseInt(codend);
        } catch (Exception e) {
            messages.add(new Message(Message.Type.BUSINESS, e.getMessage()));
            daoSession.getErroDao().insert(new Erro(e.toString()));
            e.printStackTrace();
        }

        return -1;
    }

    private int consultarBairro(String nomeBairro) throws Exception {
        if (nomeBairro == null)
            throw new SankhyaException("Nome do bairro não preenchido.");

        String nomeBai = nomeBairro.toUpperCase();

        //XML para consultar se a cidade existe, se existir vai retornar o código da mesma (CODCID)
        StringBuilder xml = new StringBuilder("<entity name='Bairro' rowsLimit='1' getPresentations='false'>");
        xml.append("<criterio nome='NOMEBAI' valor='");
        xml.append(nomeBai);
        xml.append("' />");
        xml.append("<fields>");
        xml.append("<field name='CODBAI'/>");
        xml.append("</fields>");
        xml.append("</entity>");

        try {
            Document docRet = serviceInvoker.call("crud.find", "mge", xml.toString());
            //printDocument(docRet);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile("/serviceResponse/responseBody/entidades/entidade/CODBAI");
            String codbai = (String) expr.evaluate(docRet, XPathConstants.STRING);

            if ("".equals(codbai)) {
                //System.out.println("Cidade não encontrada");

                //XML para inserir um novo bairro
                xml = new StringBuilder("<entity name='Bairro'>");
                xml.append("<campo nome='NOMEBAI'>");
                xml.append(nomeBai);
                xml.append("</campo>");
                xml.append("<campo nome='DESCRICAOCORREIO'>");
                xml.append(nomeBai);
                xml.append("</campo>");
                xml.append("</entity>");

                docRet = serviceInvoker.call("crud.save", "mge", xml.toString());
                //printDocument(docRet);

                codbai = (String) expr.evaluate(docRet, XPathConstants.STRING);
                //System.out.println("Bairro cadastrado com sucesso! Código : " + codbai);
            }
            /*else {
                System.out.println("Bairro existente: " + codbai);
            }*/

            return Integer.parseInt(codbai);
        } catch (Exception e) {
            messages.add(new Message(Message.Type.BUSINESS, e.getMessage()));
            daoSession.getErroDao().insert(new Erro(e.toString()));
            e.printStackTrace();
        }

        return -1;
    }

    private int consultarCidade(String nomeCidade, int uf) throws Exception {
        if (nomeCidade == null) {
            throw new SankhyaException("Nome da cidade não empreenchido.");
        }

        String nomecid = (nomeCidade!=null)?nomeCidade.toUpperCase():Util.STRING_EMPTY;

        //XML para consultar se a cidade existe, se existir vai retornar o código da mesma (CODCID)
        StringBuilder xml = new StringBuilder("<entity name='Cidade' rowsLimit='1' getPresentations='false'>");
        xml.append("<criterio nome='NOMECID' valor='");
        xml.append(nomecid);
        xml.append("' />");
        xml.append("<criterio nome='UF' valor='");
        xml.append(uf);
        xml.append("' />");
        xml.append("<fields>");
        xml.append("<field name='CODCID'/>");
        xml.append("</fields>");
        xml.append("</entity>");

        try {
            Document docRet = serviceInvoker.call("crud.find", "mge", xml.toString());
            //printDocument(docRet);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile("/serviceResponse/responseBody/entidades/entidade/CODCID");
            String codcid = (String) expr.evaluate(docRet, XPathConstants.STRING);

            if ("".equals(codcid)) {
                //XML para inserir uma nova cidade
                xml = new StringBuilder("<entity name='Cidade'>");
                xml.append("<campo nome='NOMECID'>");
                xml.append(nomecid);
                xml.append("</campo>");
                xml.append("<campo nome='UF'>");
                xml.append(uf);
                xml.append("</campo>");
                xml.append("</entity>");

                docRet = serviceInvoker.call("crud.save", "mge", xml.toString());
                //printDocument(docRet);

                codcid = (String) expr.evaluate(docRet, XPathConstants.STRING);
                //System.out.println("Cidade cadastrada com sucesso! Código : " + codcid);
            }
            /*else {
                System.out.println("Cidade existente: " + codcid);
            }*/

            return Integer.parseInt(codcid);
        } catch (Exception e1) {
            messages.add(new Message(Message.Type.BUSINESS, e1.getMessage()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            e1.printStackTrace();
        }

        return -1;
    }
}
