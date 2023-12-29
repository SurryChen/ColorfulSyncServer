package com.somecode.colorfulsync;

import com.alibaba.fastjson2.JSON;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {

    /**
     * 所有连接IP - 键值对是（设备ID，节点信息）
     */
    static Map<String, Node> deviceConnect = new HashMap<>();

    /**
     * 套接字连接
     */
    static DatagramSocket server;

    /**
     * 端口
     */
    static int PORT = 8002;

    public static void main(String[] args) throws IOException {
        // 绑定端口
        server = new DatagramSocket(PORT);
        // 缓冲区
        byte[] buf = new byte[1024];
        // 给UDP接收包设置缓冲区
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // 循环监听
        while (true) {
            // 监听数据，并放入到packet中
            server.receive(packet);
            // 一个UDP包最大是64k，这里应该还不会变得太大
            String requestMessage = new String(packet.getData(), 0, packet.getLength());
            // 打印
            System.out.println("收到数据包：" + requestMessage);
            try {
                // 反序列化指令
                Command command = JSON.parseObject(requestMessage, Command.class);
                // 获取IP和端口
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                // 判断
                if (command.type == 1) {
                    heartbeatProcessor(command, address, port);
                } else if (command.type == 2) {
                    requestTargetDeviceProcessor(command, address, port);
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 心跳指令处理器
     * @param command
     * @param address
     * @param port
     */
    private static void heartbeatProcessor(Command command, InetAddress address, int port) {
        // 是否有连接
        if (deviceConnect.get(command.sourceDeviceID) == null) {
            // 创建一个Node节点
            Node node = new Node();
            node.ip = address.getHostAddress();
            node.port = port;
            node.lastHeartBeatTimestamp = new Date().getTime();
            node.deviceID = command.sourceDeviceID;
            deviceConnect.put(command.sourceDeviceID, node);
        } else {
            // 更新最后一次心跳时间
            Node node = deviceConnect.get(command.sourceDeviceID);
            node.lastHeartBeatTimestamp = new Date().getTime();
        }
    }

    /**
     * 请求目标IP处理器
     * @param command
     * @param address
     * @param port
     */
    private static void requestTargetDeviceProcessor(Command command, InetAddress address, int port) {
        // 返回节点信息
        Node node = null;
        // 查看缓存
        if (deviceConnect.get(command.targetDeviceID) != null) {
            // 创建一个空的告诉对方没有或者连接超时
            Node findNode = deviceConnect.get(command.targetDeviceID);
            long nowTime = new Date().getTime();
            if (nowTime - findNode.lastHeartBeatTimestamp < 3000) {
                node = findNode;
            }
        }
        // 如果没有对应的信息，创建一个空的进行返回
        if (node == null) {
            node = new Node();
        }
        String returnMessage = JSON.toJSONString(node);
        System.out.println("返回的信息：" + returnMessage + "；返回的地址：" + address.getHostAddress() + ":" + port);
        byte[] bytes = returnMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, address, port);
        try {
            server.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 节点信息
     */
    static class Node {
        /**
         * 设备ID
         */
        String deviceID = "";

        /**
         * 节点IP
         */
        String ip = "";

        /**
         * 节点端口
         */
        int port;

        /**
         * 最后一次心跳的时间戳
         */
        long lastHeartBeatTimestamp;

        public void setDeviceID(String deviceID) {
            this.deviceID = deviceID;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setLastHeartBeatTimestamp(long lastHeartBeatTimestamp) {
            this.lastHeartBeatTimestamp = lastHeartBeatTimestamp;
        }

        public String getDeviceID() {
            return deviceID;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public long getLastHeartBeatTimestamp() {
            return lastHeartBeatTimestamp;
        }
    }

    /**
     * 指令信息
     */
    static class Command {
        /**
         * 指令类型 - 1：心跳；2：请求指定设备的IP
         */
        int type;

        /**
         * 源设备的ID
         */
        String sourceDeviceID;

        /**
         * 目标设备的ID
         */
        String targetDeviceID;

        public void setType(int type) {
            this.type = type;
        }

        public void setSourceDeviceID(String sourceDeviceID) {
            this.sourceDeviceID = sourceDeviceID;
        }

        public void setTargetDeviceID(String targetDeviceID) {
            this.targetDeviceID = targetDeviceID;
        }

        public int getType() {
            return type;
        }

        public String getSourceDeviceID() {
            return sourceDeviceID;
        }

        public String getTargetDeviceID() {
            return targetDeviceID;
        }
    }

}
