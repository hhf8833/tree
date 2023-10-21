package com.hhf.treeblocknode.server;

import com.alibaba.fastjson2.JSON;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
@Setter
@Getter
public class CertificationManagement {
    //public static String cerFilePath = "D:\\Javaproject\\tree\\treeblocknode\\src\\main\\resources\\KEY.keystore";
    public static String cerFilePath = "./KEY.keystore";
    private PrivateKey privateKey;
    private X509Certificate certificate;
    private PublicKey publicKey;
    private String signature;
    private FileInputStream input;
    public CertificationManagement() {

        //载入 jks 和该 jks 的密码 到 KeyStore 内
        try {
             input  = new FileInputStream(cerFilePath);
            //以 PKCS12 规格，创建 KeyStore
             KeyStore keyStore = KeyStore.getInstance("PKCS12");
            //File file = new ClassPathResource(cerFilePath).getFile();
            //keyStore.load(new FileInputStream(new ClassPathResource(cerFilePath).getFile()), "123456".toCharArray());
            keyStore.load(input, "123456".toCharArray());

            // 获取 keyStore 内所有别名 alias
            Enumeration<String> aliases = keyStore.aliases();
            String alias = null;
            alias = aliases.nextElement();

            System.out.println("jks文件别名是：" + alias);
            char[] keyPassword = "123456".toCharArray();

            privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword);
            System.out.println("===============private  Key==================\n" + new String(Base64.getEncoder().encode(privateKey.getEncoded())));
            certificate = (X509Certificate)keyStore.getCertificate(alias);
            System.out.println("===============certificate=================\n" + new String(Base64.getEncoder().encode(certificate.getEncoded())));
            publicKey = certificate.getPublicKey();
            System.out.println("==============public  Key===============\n" + new String(Base64.getEncoder().encode(publicKey.getEncoded())));
             signature = new String(Base64.getEncoder().encode(certificate.getSignature()));

            System.out.println("==============签名为===============\n" + signature);

            String s = new String(Base64.getEncoder().encode(certificate.getEncoded()));
            byte[] decode = Base64.getDecoder().decode(s.getBytes());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(decode);
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
            System.out.println("------"+cert);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws KeyStoreException {
        CertificationManagement certificationManagement = new CertificationManagement();
        FileInputStream input = certificationManagement.getInput();
        byte[] bytes = JSON.toJSONBytes(input);
        FileInputStream input2 = JSON.parseObject(bytes, FileInputStream.class);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        //File file = new ClassPathResource(cerFilePath).getFile();
        //keyStore.load(new FileInputStream(new ClassPathResource(cerFilePath).getFile()), "123456".toCharArray());
        try {
            keyStore.load(input, "123456".toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        System.out.println(keyStore.getCertificate("key"));
    }
}
