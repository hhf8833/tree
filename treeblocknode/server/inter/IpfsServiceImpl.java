package com.hhf.treeblocknode.server.inter;

import com.hhf.treeblocknode.server.inter.IpfsService;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.ipfs.multihash.Multihash;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class IpfsServiceImpl implements IpfsService {

    // ipfs 的服务器地址和端口，与yaml文件中的配置对应
    @Value("${ipfs.config.multi-addr}")
    private String multiAddr;

    private IPFS ipfs;

    @PostConstruct
    public void setMultiAddr() {
        ipfs = new IPFS(multiAddr);
    }

    @Override
    public String uploadToIpfs(String block) throws IOException {
        //NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File(filePath));

        MerkleNode addResult = ipfs.block.put(Collections.singletonList(block.getBytes(StandardCharsets.UTF_8))).get(0);
        //System.out.println(addResult.toString());
        return addResult.hash.toString();
    }

    @Override
    public String uploadToIpfs(byte[] data) throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(data);
        MerkleNode addResult = ipfs.add(file).get(0);
        return addResult.hash.toString();
    }

    @Override
    public byte[] downFromIpfs(String hash) {
        byte[] data = null;
        try {
            Multihash multihash = Multihash.fromBase58(hash);

            data = ipfs.block.get(multihash);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("数据接收："+ data);
        return data;
    }

    @Override
    public void downFromIpfs(String hash, String destFile) {
        byte[] data = null;
        try {
            data = ipfs.cat(Multihash.fromBase58(hash));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data != null && data.length > 0) {
            File file = new File(destFile);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}