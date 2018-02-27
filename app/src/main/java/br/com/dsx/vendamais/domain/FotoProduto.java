    package br.com.dsx.vendamais.domain;

    import org.greenrobot.greendao.annotation.Entity;
    import org.greenrobot.greendao.annotation.Generated;
    import org.greenrobot.greendao.annotation.Id;
    import org.greenrobot.greendao.annotation.Property;

    import java.io.Serializable;

    @Entity(nameInDb = "FOTO_PRODUTO")
    public class FotoProduto implements Serializable {

        private static final long serialVersionUID = 699012239470004172L;

        @Id
        @Property(nameInDb = "ID_FOTO_PRODUTO")
        private Long id;

        @Property(nameInDb = "CONTEUDO")
        private byte[] conteudo;

        @Property(nameInDb = "ID_PRODUTO")
        private Long idProduto;

        public FotoProduto() {
        }

        @Generated(hash = 202609600)
        public FotoProduto(Long id, byte[] conteudo, Long idProduto) {
            this.id = id;
            this.conteudo = conteudo;
            this.idProduto = idProduto;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public byte[] getConteudo() {
            return conteudo;
        }

        public void setConteudo(byte[] conteudo) {
            this.conteudo = conteudo;
        }

        public Long getIdProduto() {
            return idProduto;
        }

        public void setIdProduto(Long idProduto) {
            this.idProduto = idProduto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FotoProduto that = (FotoProduto) o;
            return getId().equals(that.getId());

        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }
    }
