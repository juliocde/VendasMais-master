package br.com.dsx.vendamais.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;

import java.util.Stack;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.fragment.BaseFragment;
import br.com.dsx.vendamais.fragment.ClienteFragmentList;
import br.com.dsx.vendamais.fragment.ConfiguracaoFragment;
import br.com.dsx.vendamais.fragment.PedidoFragmentList;
import br.com.dsx.vendamais.fragment.ProdutoFragmentList;
import br.com.dsx.vendamais.fragment.SincronizarFragment;

public class PrincipalActivity extends MainActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private MenuItem searchMenuItem;
    private FragmentManager.OnBackStackChangedListener onBackStackChangedListener;
    private Perfil perfil;
    private Stack<String> titles;
    private BaseFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PrincipalActivity activity = this;
        setContentView(R.layout.principal_activity);

        Intent intent = activity.getIntent();
        perfil = (Perfil) intent.getSerializableExtra(Constants.Key.PERFIL);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerClosed(View view) {
                configuraBackStack();
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                drawerToggle.setDrawerIndicatorEnabled(true);
                super.onDrawerOpened(drawerView);
            }
        };

        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
        drawerLayout.setDrawerListener(drawerToggle);


        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        TextView nomeUsuario = (TextView) nav.getHeaderView(0).findViewById(R.id.nomeUsuarioNav);
        nomeUsuario.setText(perfil.getNome());
        TextView emailUsuario = (TextView) nav.getHeaderView(0).findViewById(R.id.emailUsuarioNav);
        emailUsuario.setText(perfil.getEmail());
        SimpleDraweeView fotoImageView = (SimpleDraweeView) nav.getHeaderView(0).findViewById(R.id.fotoImageView);
        ImageView semImageView = (ImageView) nav.getHeaderView(0).findViewById(R.id.semImageView);
        if (Util.isNotBlank(perfil.getFotoUri())){
            Uri imageUri = Uri.parse(perfil.getFotoUri());
            fotoImageView.setImageURI(imageUri);
            fotoImageView.setVisibility(View.VISIBLE);
            semImageView.setVisibility(View.GONE);
        }else{
            fotoImageView.setVisibility(View.GONE);
            semImageView.setVisibility(View.VISIBLE);
        }

        onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                configuraBackStack();
            }
        };
        getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        configuraBackStack();

        exibeFragmentSelecionado(R.id.nav_pedidos);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Util.hideKeyboard(this);
            super.onBackPressed();
            if (!getTitles().isEmpty()) {
                String title = getTitles().pop();
                getSupportActionBar().setTitle(title);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.principal_bar_item, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint("Pesquisar");
        searchMenuItem.setIcon(Util.changeDrawableTint(searchMenuItem.getIcon(), Color.WHITE));

        MenuItem salvarMenuItem = menu.findItem(R.id.action_salvar);
        salvarMenuItem.setIcon(Util.changeDrawableTint(salvarMenuItem.getIcon(), Color.WHITE));

        MenuItem selecionarMenuItem = menu.findItem(R.id.action_selecionar);
        selecionarMenuItem.setIcon(Util.changeDrawableTint(selecionarMenuItem.getIcon(), Color.WHITE));

        ((VendaMaisApplication)getApplication()).setMenu(menu);

        if (fragment!=null) {
            fragment.configuraMenu(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        exibeFragmentSelecionado(item.getItemId());
        return true;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);
        super.onDestroy();
    }

    /**
     * Exibe o fragmento selecionado.
     *
     * @param itemId Identificador do fragmento escolhido pelo usuário
     */
    private void exibeFragmentSelecionado(int itemId) {

        DaoSession daoSession = ((VendaMaisApplication)getApplication()).getDaoSession();

        String title = Util.STRING_EMPTY;
        switch (itemId) {
            case R.id.nav_clientes:
                fragment = new ClienteFragmentList();
                break;
            case R.id.nav_produtos:
                fragment = new ProdutoFragmentList();
                break;
            case R.id.nav_sincronizar:
                fragment = new SincronizarFragment();
                break;
            case R.id.nav_configuracao:
                fragment = new ConfiguracaoFragment();
                break;
            case R.id.nav_logout:
                logout();
                break;
            default:
                fragment = new PedidoFragmentList();
        }
        if (fragment != null) {
            getTitles().clear();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }



    /**
     * Configura a possibilidade voltar de um fragment para outro (uma parte).
     */
    private void configuraBackStack() {

        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackEntryCount>0){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        else{
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        drawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
    }


    private void logout(){
        if (perfil.getTipo()!=null) {
            switch (perfil.getTipo()) {
                case Constants.TipopPerfil.FACEBOOK:
                    LoginManager.getInstance().logOut();
                default:
                    perfil.setLogado(false);
                    perfilDao.insertOrReplace(perfil);
            }
            setPerfil(perfil);
        }
        //Remover este comando de delete após concluir os testes
        //perfilDao.deleteAll();
        finishAffinity();
    }

    public Stack<String> getTitles() {
        if (titles==null) {
            titles = new Stack<>();
        }
        return titles;
    }

}
