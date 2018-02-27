package br.com.dsx.vendamais.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Email;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.domain.Perfil;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends MainActivity {

    @BindView(R.id.loginUsuario) TextView txtEmail;
    @BindView(R.id.senhaUsuario) TextView txtSenha;
    @BindView(R.id.socialButtom) LinearLayout layoutSocial;
    @BindView(R.id.formLogin) RelativeLayout layoutForm;
    @BindView(R.id.msgRegistrar) LinearLayout layoutRegistrar;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.checkboxRemember) AppCompatCheckBox lembrar;

    private Perfil perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        initPerfil();
    }

    private void initPerfil() {
        perfil = getPerfil();
        if (perfil.isLogado()) {
            startApp(perfil);
        } else if (perfil.isLembrarSenha()){
            txtEmail.setText(perfil.getLogin());
            txtSenha.setText(perfil.getSenha());
            lembrar.performClick();
        }
    }

    @OnClick(R.id.loginButton)
    void login(){
        if (validaCampos()) {
            perfil.setTipo(Constants.TipopPerfil.APP);
            perfil.setLembrarSenha(lembrar.isChecked());
            perfil.setLogado(true);
            perfilDao.insertOrReplace(perfil);
            setPerfil(perfil);
            startApp(perfil);
        }
    }

    private boolean validaCampos() {
        boolean valido = true;
        if (Util.isBlank(txtEmail.getText().toString())) {
            txtEmail.setError("Email é obrigatório.");
            valido = false;
        } else if (!Util.isValidEmail(txtEmail.getText())) {
            txtEmail.setError("Email inválido.");
            valido = false;
        } else if (perfil.getId()==null) {
            txtEmail.setError("Email não cadastrado, registre-se.");
            valido = false;
        }
        if (Util.isBlank(txtSenha.getText().toString())) {
            txtSenha.setError("Senha é obrigatório.");
            valido = false;
        } else if (valido && !txtSenha.getText().toString().equals(perfil.getSenha())) {
            txtSenha.setError("Senha não confere.");
            valido = false;
        }
        return valido;
    }


    @OnClick(R.id.cadastrarUsuario)
    void cadastrar(){
        Intent intent = new Intent(this, CadastroActivity.class);
        intent.putExtra(Constants.Key.PERFIL, perfil);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.esqueceuSenha)
    void esqueceuSenha(){
        if (validaEnvio()) {
            String email = perfil.getEmail();
            String subject = "Venda Mais - Senha";
            String message = "Senha: "+perfil.getSenha();
            String popupMessage = "Enviando senha para o email "+email;
            Email sendEmail = new Email(this, email, subject, message, popupMessage,
                    new Email.EmailResponse() {
                        @Override
                        public void processFinish(String erroMsg) {
                            if (Util.isNotBlank(erroMsg)) {
                                Toast.makeText(txtEmail.getContext(),
                                        "Verifique sua conexão com a internet e tente novamente.",Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(txtEmail.getContext(),
                                        "Senha enviada com sucesso.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            sendEmail.execute();
            Util.hideKeyboard(this);
        }
    }

    private boolean validaEnvio() {
        boolean valido = true;
        if (Util.isBlank(perfil.getEmail())) {
            Toast.makeText(txtEmail.getContext(),
                    "Usuário não cadastrado, registre-se.",Toast.LENGTH_LONG).show();
            valido = false;
        }
        return valido;
    }

}

