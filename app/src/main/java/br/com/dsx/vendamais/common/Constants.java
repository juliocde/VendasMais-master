package br.com.dsx.vendamais.common;

/**
 * Definir constantes para a aplicação.
 */
public interface Constants {

    interface Query {
        int LIMITE_CLIENTES = 50;
        int LIMITE_PRODUTOS = 150;
    }

    interface TipoCliente {
        int PESSOA_FISICA = 1;
        int PESSOA_JURIDICA = 0;
    }

    interface TipopPerfil {
        int FACEBOOK = 10;
        int GOOGLE_PLUS = 20;
        int APP = 30;
    }

    interface Key {
        String CLIENTE_LISTENER = "CLIENTE_LISTENER";
        String CLIENTE_FRAG_LIST_LISTENER = "CLIENTE_FRAG_LIST_LISTENER";
        String FORMA_PAGMENTO = "FORMA_PAGMENTO";
        String FORMA_PAGMENTO_LISTENER = "FORMA_PAGMENTO_LISTENER";
        String TIPO_TITULO_LISTENER = "TIPO_TITULO_LISTENER";
        String EMPRESA = "EMPRESA";
        String EMPRESA_LISTENER = "EMPRESA_LISTENER";
        String LOCAL_ESTOQUE = "LOCAL_ESTOQUE";
        String LOCAL_ESTOQUE_LISTENER = "LOCAL_ESTOQUE_LISTENER";
        String PRODUTO_LISTENER = "PRODUTO_LISTENER";
        String FRAGMENT_LISTENER = "FRAGMENT_LISTENER";
        String CLIENTE = "CLIENTE";
        String PRODUTO = "PRODUTO";
        String FOTO_PRODUTO = "FOTO_PRODUTO";
        String ITENS_PEDIDO = "ITENS_PEDIDO";
        String MODO = "MODO";
        String PERFIL = "PERFIL";
        String ID_EMPRESA = "ID_EMPRESA";
        String ID_LOCAL_ESTOQUE = "ID_LOCAL_ESTOQUE";
        String TAXA = "TAXA";
        String PEDIDO = "PEDIDO";
        String PEDIDO_FRAG_LIST_LISTENER= "PEDIDO_FRAG_LIST_LISTENER";
    }

    interface Mask{
        String CELULAR= "[00] [000000000]";
        String TELEFONE= "[00] [00000000]";
        String CPF= "[000].[000].[000]-[00]";
        String CNPJ= "[00].[000].[000]/[0000]-[00]";
        String CEP= "[00].[000]-[000]";
    }

    interface SyncStatus {
        String SUCESSO = "sincronizado.";
        String FALHA = "falha na sincronização.";
    }

    interface RequestCode {
        int PRODUTO = 1;
        int CLIENTE = 2;
    }

    interface ConfiguracaoIds {
        Long DATA_ULT_SINC = 1L;
        Long CODIGO_VEND = 2L;
        Long COD_VEND = 2L;
    }

    interface DateFormat {
        String DATA = "dd/MM/yyyy";
        String DATA_HORA = "dd/MM/yyyy HH:mm";
        String DATA_HORA_BANCO = "yyyy/MM/dd HH:mm";
    }

    interface Modo {
        String SELECAO = "SELECAO";
        String EDICAO = "EDICAO";
    }

}
