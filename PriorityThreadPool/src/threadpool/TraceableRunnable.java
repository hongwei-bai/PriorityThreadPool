package threadpool;

abstract public class TraceableRunnable implements Runnable {
    private Object[] paras;

    abstract public int getParaCount();

    public void setParas(Object... inparas) {
        paras = new Object[getParaCount()];
        for (int i = 0; i < getParaCount(); i++) {
            this.paras[i] = inparas[i];
        }
    }

    public Object getPara(int i) {
        return paras[i];
    }

    public Object[] getParas() {
        return paras;
    }
}
