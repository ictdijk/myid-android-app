package in.yagnyam.myid.data;


public interface DataSetObserver<T> {
    void itemInserted(int pos);
    void itemChanged(int pos);
    void itemRemoved(int pos);
}
