package com.somecode.colorfulsync;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("server start");

        // 绑定端口
        DatagramSocket server = new DatagramSocket(8002);
        // 缓冲区
        byte[] buf = new byte[1024];
        // 数据包
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // 节点数量
        int count = 0;
        // 数量的字符串形式
        String count_S;
        // 节点A和节点B的出口IP
        String clientA_IP = "", clientB_IP = "";
        // 节点A和节点B的IP:post拼接的字符串
        String ClientA = "", ClientB = "";
        // 节点A和节点B的出口端口
        int clientA_port = 0, clientB_port = 0;
        //
        InetAddress clientA_address = null, clientB_address = null;
        // 循环监听
        while (true) {
            // 监听数据，并放入到packet中
            server.receive(packet);
            // 拿出数据
            String requestMessage = new String(packet.getData(), 0, packet.getLength());
            // 输出数据
            System.out.println(requestMessage);
            // requestMessage类似于指令
            if (requestMessage.contains("Request ID")) {
                // 客户端+1
                count++;
                // 字符串形式
                count_S = String.valueOf(count);
                // 判断id的单双
                if (count % 2 != 0) {
                    // 单数填充A
                    clientA_port = packet.getPort();
                    clientA_address = packet.getAddress();
                    ClientA = count_S + ":" + clientA_address.getHostAddress() + ":" + clientA_port;
                    // 输出A的信息
                    System.out.println("client " + ClientA);
                    clientA_IP = clientA_address.getHostAddress() + ":" + clientA_port;
                    // 分配ID
                    sendID(count_S, clientA_port, clientA_address, server);
                } else {
                    // 偶数填充B
                    clientB_port = packet.getPort();
                    clientB_address = packet.getAddress();
                    ClientB = count_S + ":" + clientB_address.getHostAddress() + ":" + clientB_port;
                    System.out.println("client " + ClientB);
                    clientB_IP = clientB_address.getHostAddress() + ":" + clientB_port;
                    // 分配ID
                    sendID(count_S, clientB_port, clientB_address, server);
                    // 延时预防丢包问题
                    try {
                        // 延时1秒
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.out.println("Got an exception!");
                    }
                    // 异步给新节点与当前已读节点发送对方的的节点信息
                    sendIP(ClientA, clientB_port, clientB_address, server);
                    sendIP(ClientB, clientA_port, clientA_address, server);
                }
            }
        }
    }

    private static void sendID(String id, int port, InetAddress address, DatagramSocket server) {// 发送ID
        // 返回id
        byte[] sendBuf = id.getBytes();
        // 创建一个upd包，制定了大小、ip、端口
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        // 发送回去
        try {
            server.send(sendPacket);
            System.out.println("ID assignment successful!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendIP(String Client, int port, InetAddress address, DatagramSocket server) {// 发送节点信息
        // 返回ip和端口
        byte[] sendBuf = Client.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        try {
            server.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static ArrayList<String> c = new ArrayList<String>();
//    public static void main(String[] args) throws IOException, InterruptedException {
//        DatagramSocket server = new DatagramSocket(8002);
//        byte[] bytes = new byte[1024];
//        while (true) {
//            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
//            server.receive(packet);
//            InetAddress address = packet.getAddress();
//            int port = packet.getPort();
//            String s = address.getHostAddress()+":"+port;
//            System.out.println("收到->"+s+"的消息：");
//
//            if (!c.contains(s)){
//                c.add(s);
//            }
//
//            for (String s1 : c) {
//                if (!s1.equals(s)){
//                    byte[] b = ("this is server:"+s1).getBytes();
//                    DatagramPacket packet1 = new DatagramPacket(b, b.length, address,port);
//                    server.send(packet1);
//                }
//            }
//        }
//    }

}
