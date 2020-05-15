package lewis.com.carpart.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by Administrator on 2019/5/7.
 */

public class Img extends BmobObject {
    public String account;
    public String lat;
    public String log;
    public String address;
    public BmobFile imgFile;
}
