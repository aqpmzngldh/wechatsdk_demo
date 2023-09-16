package cn.qianyekeji.ruiji.entity;

import com.thoughtworks.xstream.XStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 微信返回用户消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    //接收方账号（收到的OpenID）
    private String ToUserName;
    //开发者微信号
    private String FromUserName;
    //消息创建时间 （整型）
    private long CreateTime;
    //消息类型，文本为text
    private String MsgType;
    //回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
    private String Content;

    //因为回复图片消息的时候image标签底下还有MediaId标签，所以这样做
    private Image Image;

    public Image getImage() {
        return Image;
    }
    public void setImage(Image image) {
        this.Image = image;
    }
    public static class Image {
        private String MediaId;
        public String getMediaId() {
            return MediaId;
        }
        public void setMediaId(String mediaId) {
            MediaId = mediaId;
        }
    }

    //因为回复图片消息的时候voice标签底下还有MediaId标签，所以这样做
    private Voice Voice;

    public Voice getVoice() {
        return Voice;
    }
    public void setVoice(Voice voice) {
        this.Voice = voice;
    }
    public static class Voice {
        private String MediaId;
        public String getMediaId() {
            return MediaId;
        }
        public void setMediaId(String mediaId) {
            MediaId = mediaId;
        }
    }




    public static String objectToXml(Message message){
        XStream xs=new XStream();
        xs.alias("xml",message.getClass());
        return xs.toXML(message);
    }

}
