package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class ServerThread extends Thread{
    private int port = 0;
    private ServerSocket serverSocket = null;
    private double euro = -1;
    private double usd = -1;
    private String timeStamp = "";

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket socket) {
        serverSocket = socket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public double getEur() {
        return euro;
    }

    public double getUsd() {
        return usd;
    }

    public void doFirstQuery() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String pageSourceCode = "";
            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
            HttpResponse httpGetResponse = httpClient.execute(httpGet);
            HttpEntity httpGetEntity = httpGetResponse.getEntity();
            if (httpGetEntity != null) {
                pageSourceCode = EntityUtils.toString(httpGetEntity);
            }
            if (pageSourceCode == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                return;
            } else {
                Log.i(Constants.TAG, pageSourceCode);
            }
            JSONObject content = new JSONObject(pageSourceCode);
            JSONObject usdJSON = content.getJSONObject("bpi").getJSONObject("USD");
            JSONObject eurJSON = content.getJSONObject("bpi").getJSONObject("EUR");
            this.usd = usdJSON.getDouble("rate_float");
            this.euro = eurJSON.getDouble("rate_float");
            Log.d(Constants.TAG, usd + "");
            Log.d(Constants.TAG, euro + "");
        } catch (JSONException e) {

        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        try {
            doFirstQuery();
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (ClientProtocolException clientProtocolException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + clientProtocolException.getMessage());
            if (Constants.DEBUG) {
                clientProtocolException.printStackTrace();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
