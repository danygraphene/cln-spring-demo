package com.example.clnspringdemo.config;

import cln.NodeGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.File;

@Configuration
public class ClnGrpcConfig {

    @Value("${cln.grpc.host:127.0.0.1}")
    private String host;

    @Value("${cln.grpc.port:9736}")
    private int port;

    @Value("${cln.grpc.ca-cert}")
    private String caCertPath;

    @Value("${cln.grpc.client-cert}")
    private String clientCertPath;

    @Value("${cln.grpc.client-key}")
    private String clientKeyPath;

    @Bean
    public ManagedChannel managedChannel() throws SSLException {
        SslContext sslContext = GrpcSslContexts.forClient()
                .trustManager(new File(caCertPath))
                .keyManager(new File(clientCertPath), new File(clientKeyPath))
                .build();

        return NettyChannelBuilder.forAddress(host, port)
                .sslContext(sslContext)
                .build();
    }

    @Bean
    public NodeGrpc.NodeBlockingStub nodeBlockingStub(ManagedChannel channel) {
        return NodeGrpc.newBlockingStub(channel);
    }
}
