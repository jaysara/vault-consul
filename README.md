# Accessing Secrets through Vault with Consul Backend and SpringBoot



This application demonstrates how SpringBoot can be used to access the secrets stored in the Vault with consul backend. Vault is a Harshicorp product that provides variety of functionalities to store the sensitive data like databse password, api-keys etc in encrypted format. Vault makes it easy to manage them securely in the cloud environment. 

In typical architecture, Vault will have a backend like consul,aws or any database where it will store its secret. In this project, I have configured Vault to use Consul as the backend. Please follow the steps below on how to achieve this.

#### Install Vault
  
  - Start with installing Vault (if you have vault server already running somewhere, you can skip this.)
  - Go to https://www.vaultproject.io/intro/getting-started/install.html on how to install vault locally.
  - Start you local vault server by typing 
        ``` $ ./vault server -dev ```
-   Please make note of Root - Token that gets displayed. Also set your environment variable with this token.
 ```sh
   export VAULT_TOKEN=..root token displayed in output of vault startup....
  ```
  - You can verify the Vault server is up by typing
        ``` ./vault status ```
  This should display some information about cluster name and cluster ID.
 
#### Install Consul
Installing consul is equally easy.
 - Please visit the following URL to see how to get started. (unless you have a cluster available somewhere)
https://www.consul.io/intro/getting-started/install.html
  -   Create consul config file :
  - - Since vault need to enable acl to store the secrets in the consul. We need to create a master token thatn can be used to create different ACL policies withi n consul. Create a file 'consul-config.json' with following content.
```json
{
  "acl_datacenter": "dc1",
  "acl_master_token": "b1gs33cr3t",
  "acl_default_policy": "deny",
  "acl_down_policy": "extend-cache"
}
```
For simplification, I have created "acl_master_token" on my own. There are various ways to do this.

  -   Start the cosul agent by typing, 
``` $ ./consul agent -dev -config-file=consul-config.json ```
  - Mount the consul as backedn for Vault by typing,
  ``` vault mount consul  ```
  -   Generate a management token to work with consul
  ```
  curl \
    -H "X-Consul-Token: b1gs33cr3t" \
    -X PUT \
    -d '{"Name": "sample", "Type": "management"}' \
    http://127.0.0.1:8500/v1/acl/create
You should see the new token as below.
{
  "ID": "adf4238a-882b-9ddc-4a9d-5b6758e4159e"
}
  ```
  - Next, we must configure Vault to know how to contact Consul. This is done by writing the access information:
```
./vault write consul/config/access \
    address=127.0.0.1:8500 \
    token=adf4238a-882b-9ddc-4a9d-5b6758e4159e
Success! Data written to: consul/config/access

```
  - -  If you get error in the above step, please make sure thet you have environment variable VAULT_TOKEN has the value that appears in the root token when vault server started up.

 - In this case, we've configured Vault to connect to Consul on the default port with the loopback address. We've also provided an ACL token to use with the token parameter. Vault must have a management type token so that it can create and revoke ACL tokens.

The next step is to configure a role. A role is a logical name that maps to a role used to generate those credentials. For example, lets create a "readonly" role:
```
POLICY='key "" { policy = "read" }'
$ echo $POLICY | base64 | vault write consul/roles/readonly policy=-
Success! Data written to: consul/roles/readonly
```
To generate a new set Consul ACL token, we simply read from that role:
```
$ vault read consul/creds/readonly
Key             Value
lease_id        consul/creds/readonly/c7a3bd77-e9af-cfc4-9cba-377f0ef10e6c
lease_duration  3600
token           973a31ea-1ec4-c2de-0f63-623f477c2510
```
* We should use this token in the application to read the values from Consul.
* However for this application to read the value from vault, we need to insert a value in the consul. Spring always reads the value from 'config' folder in consul. So, we need to put a value using `acl-master-token' (b1gs33cr3t  - in this example).  Type following,

```
$ curl -X PUT -d 'test' 127.0.0.1:8500/v1/kv/config/my-spring-boot-app/foo?token=b1gs33cr3t
```
- Now update the acl-token property of sprint with the on that vault generated in the previous step. Make sure following properties have the right values.
``` 
spring.cloud.consul.config.acl-token=973a31ea-1ec4-c2de-0f63-623f477c2510 
#Vault token is required for Spring to start communicating with Vault.
spring.cloud.vault.token=554f08c1-3532-f71d-3ce1-da5ea05c1d47 
#To discover a service, higher level - master token is needed.
spring.cloud.consul.discovery.acl-token=b1gs33cr3t

```
- run the application and you should see the value in the property 'foo' in the variable mykey printed out.
```java
@Value("${foo}")
String mykey; // wi	ll have value bar
```
