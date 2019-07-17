package com.shnuedu.tools;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NetMessage {
    /**
     * 消息Id
     * 01：表示手机设置
     */
    public int MsgId;

    /**
     * 消息状态
     */
    public boolean MsgStatus;
    /**
     * 消息内容
     */
    public Object MsgObj;

    /**
     * 转换成指定类型的对象
     *
     * @param classOfT
     * @param <T>
     * @return
     */
    public <T> T JsonToObject(Gson gson, Class<T> classOfT) {
        if (MsgObj == null) {
            return null;
        }
        String gsonStr = gson.toJson(MsgObj);
        T target = gson.fromJson(gsonStr, classOfT);
        return target;
    }

    /**
     * 转换成指定类型的ArrayList
     *
     * @param classOfT
     * @param <T>
     * @return
     */
    public <T> List<T> JsonToListObject(Gson gson, Class<T> classOfT) {
        if (MsgObj == null) {
            return null;
        }
        List<T> target = new ArrayList<>();
        if (MsgObj instanceof ArrayList) {
            for (Object obj : (ArrayList<T>) MsgObj) {
                //因为List<Wifi>转成Json字符串后再转回来时变成ArrayList<LinkTreeMap>
                //可以通过先把LinkTreeMap转成Json字符串然后在转成指定的对象
                String gsonStr = gson.toJson(obj);
                T t = gson.fromJson(gsonStr, classOfT);
                target.add(t);
            }
        }
        return target;
    }
}

