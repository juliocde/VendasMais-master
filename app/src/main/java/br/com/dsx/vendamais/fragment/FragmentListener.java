package br.com.dsx.vendamais.fragment;

import java.io.Serializable;

/**
 * Created by salazar on 30/06/17.
 */

public interface FragmentListener extends Serializable {
    void onFragmentEdit();
    void onFragmentFinish();
}
