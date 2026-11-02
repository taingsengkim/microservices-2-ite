package kh.edu.istad.platform.order.client.exception;

public class DownStreamServiceExecption extends RuntimeException{
    private String service;
    private DownStreamServiceExecption(String message){
        super(message);
    }
}
