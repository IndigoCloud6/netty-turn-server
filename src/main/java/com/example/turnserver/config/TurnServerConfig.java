package com.example.turnserver.config;

import com.example.turnserver.handler.TurnServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

/**
 * Configuration for the TURN server Netty components
 */
@Configuration
public class TurnServerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TurnServerConfig.class);
    
    @Value("${turn.server.port:3478}")
    private int turnPort;
    
    @Value("${stun.server.port:3478}")
    private int stunPort;
    
    @Value("${turn.server.external-ip:127.0.0.1}")
    private String externalIp;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    @Autowired
    private TurnServerHandler turnServerHandler;
    
    /**
     * TURN/STUN server bootstrap configuration
     */
    @Bean
    public Bootstrap turnServerBootstrap() {
        logger.info("Configuring TURN/STUN server bootstrap");
        
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_RCVBUF, 65536)
                .option(ChannelOption.SO_SNDBUF, 65536)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ch.pipeline().addLast("turnHandler", turnServerHandler);
                    }
                });
        
        return bootstrap;
    }
    
    /**
     * Start the TURN/STUN server when application context is ready
     */
    @EventListener
    public void startTurnServer(ContextRefreshedEvent event) {
        try {
            logger.info("Starting TURN/STUN server on port {} with external IP {}", turnPort, externalIp);
            
            Bootstrap bootstrap = turnServerBootstrap();
            bootstrap.bind(turnPort).sync().addListener(future -> {
                if (future.isSuccess()) {
                    logger.info("TURN/STUN server started successfully on port {}", turnPort);
                } else {
                    logger.error("Failed to start TURN/STUN server on port {}", turnPort, future.cause());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error starting TURN/STUN server", e);
        }
    }
    
    /**
     * Shutdown Netty event loop groups
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down TURN server");
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        logger.info("TURN server shutdown completed");
    }
    
    /**
     * Get the configured TURN server port
     */
    public int getTurnPort() {
        return turnPort;
    }
    
    /**
     * Get the configured STUN server port
     */
    public int getStunPort() {
        return stunPort;
    }
    
    /**
     * Get the configured external IP
     */
    public String getExternalIp() {
        return externalIp;
    }
}