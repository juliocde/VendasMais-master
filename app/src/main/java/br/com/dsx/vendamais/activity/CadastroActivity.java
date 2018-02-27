package br.com.dsx.vendamais.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Email;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CadastroActivity extends MainActivity {

    @BindView(R.id.labelCadastro) TextView labelCadastro;
    @BindView(R.id.labelCodigo) TextView labelCodigo;
    @BindView(R.id.formCodigo) RelativeLayout formCodigo;
    @BindView(R.id.formCadastro) RelativeLayout formCadastro;

    @BindView(R.id.nomeUsuario) TextView nomeUsuario;
    @BindView(R.id.emailUsuario) TextView emailUsuario;
    @BindView(R.id.senhaUsuario) TextView senhaUsuario;
    @BindView(R.id.senha2Usuario) TextView senha2Usuario;
    @BindView(R.id.codigoVerificacao) TextView codigoVerificacao;

    private Perfil perfil;
    private String codigoEnviado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastrar_activity);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        perfil = (Perfil) intent.getSerializableExtra(Constants.Key.PERFIL);

        labelCadastro.setVisibility(View.VISIBLE);
        formCadastro.setVisibility(View.VISIBLE);
        labelCodigo.setVisibility(View.GONE);
        formCodigo.setVisibility(View.GONE);

        senha2Usuario.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    enviarEmail();
                    return true;
                }
                return false;
            }
        });
        codigoVerificacao.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    verificarCodigo();
                    return true;
                }
                return false;
            }
        });

    }

    @OnClick(R.id.sendButton)
    void enviarEmail(){

        if (validaCampos()) {
            double numero = Math.random();
            codigoEnviado =  String.valueOf(((int)(numero*10000)));
            String email = emailUsuario.getText().toString();
            String subject = "Venda Mais - Código de Verificação";
            String message = "Código: "+codigoEnviado;
            Log.d("Cadastro: ", message);
            Email sendEmail = new Email(this, email, subject, message, new Email.EmailResponse() {
                @Override
                public void processFinish(String erroMsg) {
                    if (Util.isNotBlank(erroMsg)) {
                        Toast.makeText(labelCadastro.getContext(),
                                "Verifique sua conexão com a internet e tente novamente.",Toast.LENGTH_LONG).show();
                    }else {
                        labelCadastro.setVisibility(View.GONE);
                        formCadastro.setVisibility(View.GONE);
                        labelCodigo.setVisibility(View.VISIBLE);
                        formCodigo.setVisibility(View.VISIBLE);
                        Toast.makeText(labelCadastro.getContext(),
                                "Código enviado com sucesso.",Toast.LENGTH_LONG).show();
                    }
                }
            });
            sendEmail.execute();
            Util.hideKeyboard(this);
        }
    }

    private boolean validaCampos() {

        boolean valido = true;
        if (Util.isBlank(nomeUsuario.getText().toString())) {
            nomeUsuario.setError("Nome é obrigatório.");
            valido = false;
        }
        if (Util.isBlank(emailUsuario.getText().toString())) {
            emailUsuario.setError("Email é obrigatório.");
            valido = false;
        } else if (!Util.isValidEmail(emailUsuario.getText())) {
            emailUsuario.setError("Email inválido.");
            valido = false;
        }
        if (Util.isBlank(senhaUsuario.getText().toString())) {
            senhaUsuario.setError("Senha é obrigatório.");
            valido = false;
        }
        if (Util.isBlank(senhaUsuario.getText().toString())) {
            senha2Usuario.setError("Confirmar a senha é obrigatório.");
            valido = false;
        } else if (!senhaUsuario.getText().toString().equals(senha2Usuario.getText().toString())) {
            senhaUsuario.setError("Senhas não conferem.");
            senha2Usuario.setError("Senhas não conferem.");
            valido = false;
        }
        return valido;
    }


    @OnClick(R.id.codigoButton)
    void verificarCodigo(){
        Util.hideKeyboard(this);
        if (codigoEnviado.equals(codigoVerificacao.getText().toString())) {
            perfil.setNome(nomeUsuario.getText().toString());
            perfil.setLogin(emailUsuario.getText().toString());
            perfil.setEmail(emailUsuario.getText().toString());
            perfil.setSenha(senhaUsuario.getText().toString());
            perfil.setTipo(Constants.TipopPerfil.APP);
            perfil.setFotoUri(Util.STRING_EMPTY);
            perfil.setLogado(true);
            perfilDao.insertOrReplace(perfil);
            setPerfil(perfil);
            startApp(perfil);
        } else{
            codigoVerificacao.setError("Código não confere. Verifique seu email.");
        }
    }
}
