public class Response {
    String[] data; //1 is always null
    String error;

    Response(String[] data){
        error = null;
        this.data = data;
    }

    Response(String error){
        this.error = error;
        data = null;
    }
}
