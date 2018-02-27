package br.com.dsx.vendamais.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.Serializable;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.common.MenuConfig;
import br.com.dsx.vendamais.component.MaskEditText;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.sync.SWServiceInvoker;
import butterknife.Unbinder;

/**
 * Created by salazar on 15/03/17.
 */

public class BaseFragment extends Fragment implements Serializable {

    protected Unbinder unbinder;
    protected String oldTitle;
    protected int textDisableColor;
    protected int textEnableColor;
    protected FragmentListener callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textDisableColor = ContextCompat.getColor(this.getContext(), R.color.text_disable);
        textEnableColor = ContextCompat.getColor(this.getContext(), R.color.text_enable);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configuraMenu(true);

        if (callback!=null){
            callback.onFragmentEdit();
        }
    }

    public void configuraMenu(boolean config) {
        Menu menu = getMenu();
        if (menu!=null) {
            menu.findItem(R.id.action_salvar).setVisible(false);
            menu.findItem(R.id.action_remover).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_selecionar).setVisible(false);
            if (config) {
                configuraMenu(menu);
            }
        }
    }

    /**
     * Permite que o fragment configure o menu.
     * @param menu Menu da aplicação.
     */
    public void configuraMenu(Menu menu){

    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();

        if (callback!=null){
            callback.onFragmentFinish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_salvar).setVisible(false);
        menu.findItem(R.id.action_remover).setVisible(false);
    }

    public void addFragment (BaseFragment baseFragment) {
        if (!baseFragment.isAdded()) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, baseFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    protected void setTitle(String title){
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void tintField(TextView textView){
        int color = textEnableColor;
        if (!textView.isEnabled() || !textView.isFocusable()) {
            color = textDisableColor;
        }
        textView.setTextColor(color);
        if (textView instanceof MaskEditText) {
            MaskEditText maskEdit = (MaskEditText) textView;
            maskEdit.setFloatingLabelTextColor(color);
        }
        if (textView instanceof MaterialBetterSpinner) {
            MaterialBetterSpinner betterSpinner = (MaterialBetterSpinner) textView;
            betterSpinner.setFloatingLabelTextColor(color);
        }


        /*
        if (!textView.isEnabled() || !textView.isFocusable()) {
            textView.setTextColor(textDisableColor);
            if (textView instanceof MaskEditText) {
                MaskEditText maskEdit = (MaskEditText) textView;
                maskEdit.setFloatingLabelTextColor(textDisableColor);
            }
        }*/
    }

    protected DaoSession getDaoSession(){
        return ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
    }

    protected Perfil getPerfil(){
        return ((VendaMaisApplication)getActivity().getApplication()).getPerfil();
    }

    protected void setPerfil(Perfil perfil) {
        ((VendaMaisApplication)getActivity().getApplication()).setPerfil(perfil);
    }

    protected MenuConfig getMenuConfig(String fragmentKey){
        return ((VendaMaisApplication)getActivity().getApplication()).getMenuConfig(fragmentKey);
    }

    protected Menu getMenu(){
        return ((VendaMaisApplication)getActivity().getApplication()).getMenu();
    }

    protected SWServiceInvoker getSankhyaService(){
        return ((VendaMaisApplication)getActivity().getApplication()).getSankhyaService();
    }

}
