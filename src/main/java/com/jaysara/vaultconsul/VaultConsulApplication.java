package com.jaysara.vaultconsul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableDiscoveryClient
//@VaultPropertySource("secret/application")
@EnableConfigurationProperties(PropertySourceBootstrapProperties.class)
public class VaultConsulApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaultConsulApplication.class, args);
	}
	@Autowired
	ConsulDiscoveryProperties properties;

	@Autowired
	ConsulAutoConfiguration consulAutoConfiguration;

	//@VaultPropertySource()
	@Autowired
	private VaultOperations vaultOperations;

	@Autowired
	VaultTemplate vaultTemplate;

	@Value("${mykey}")
	String mykey; // wi	ll have value bar

	@PostConstruct
	private void postConstruct() throws Exception {

		System.out.println("##########################");
		System.out.println("Generated token: " + properties.getAclToken());
		//System.out.println(" Properties are "+properties);
	//	vaultOperations.write("kv/config/application","{\"springfoo\":\"mspringbar\"}");
		VaultResponse readprop = vaultOperations.read("foo");
		System.out.println("Write vault "+vaultTemplate);
		System.out.println( "Valut list path : "+vaultTemplate.list("/"));

		System.out.println( "Reading from Vault is "+readprop);
		//consulAutoConfiguration.consulProperties().
		System.out.println("##########################" + properties);
		System.out.println("Value of mykey is "+mykey);
		//System.out.println( " The value of foo is "+foo);
	}


}
