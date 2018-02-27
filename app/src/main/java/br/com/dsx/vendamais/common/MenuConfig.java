package br.com.dsx.vendamais.common;

import android.view.Menu;

import java.io.Serializable;

/**
 * Created by salazar on 04/06/17.
 */

public class MenuConfig implements Serializable {

    private boolean searchViewVisible;
    private boolean searchCollapse;
    private String searchFilter;

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public boolean isSearchCollapse() {
        return searchCollapse;
    }

    public void setSearchCollapse(boolean searchCollapse) {
        this.searchCollapse = searchCollapse;
    }

    public boolean isSearchViewVisible() {
        return searchViewVisible;
    }

    public void setSearchViewVisible(boolean searchViewVisible) {
        this.searchViewVisible = searchViewVisible;
    }
}
