package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import com.cxf.logger.Logs;
import com.cxf.util.ByteUtil;

public class TcpDecode extends MessageToMessageDecoder<ByteBuf> {

  @Override
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    try {
      int length = in.readableBytes();
      byte[] array = new byte[length];
      in.getBytes(0, array);
      Logs.TCP.warn("receive msg:" + ByteUtil.printHexString(array));

      ByteBuf cmdBytes = in.slice(0, 1), lenBytes = in.slice(1, 2);

      byte[] cmds = new byte[1], lens = new byte[2];

      cmdBytes.readBytes(cmds);
      lenBytes.readBytes(lens);

      int len = ByteUtil.byteArrayToInt(lens, 2);
      byte[] newArray;
      ByteBuf msgByte;

      switch (cmds[0]) {
        case 0x01:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;
        case 0x05:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;

        case (byte) 0xBE:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;

        case (byte) 0xBD:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;
        case (byte) 0xBC:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;
        case (byte) 0x08:
          newArray = new byte[4 + len];
          msgByte = in.slice(0, 4 + len);
          msgByte.readBytes(newArray);
          out.add(newArray);
          break;
        default:
          // in.skipBytes(5 + len);
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
