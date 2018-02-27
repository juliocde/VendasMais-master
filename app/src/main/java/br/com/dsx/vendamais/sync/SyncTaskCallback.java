package br.com.dsx.vendamais.sync;

/**
 * Created by salazar on 14/03/17.
 */
public interface SyncTaskCallback<T> {
    void processFinish(T values);
    void processCanceled(T reasons);
}
