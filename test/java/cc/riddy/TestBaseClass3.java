package cc.riddy;

public abstract class TestBaseClass3 {
    public double getDouble(int a, double b, String c, Integer d, boolean e) {
        if(e) {
            return (double) a + b + d;
        } else {
            return 0.0;
        }
    }

    public String getString(String a) {
        return a;
    }

    public Integer getInt(){
        return 100;
    }

    public int getOtherInt(){
        return 200;
    }

    public int trickyCase(int a, String b) {
        return 6;
    }

    public int trickyCase(java.lang.Integer a, String b) {
        return 7;
    }
}
