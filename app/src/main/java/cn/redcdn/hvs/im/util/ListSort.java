package cn.redcdn.hvs.im.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;

import com.butel.connectevent.utils.LogUtil;

/**
 * List对象排序的通用方法
 *
 * @author chencj
 *
 * @param <E>
 */
public class ListSort<E> {
    /**
     *
     * @param list
     *            要排序的集合
     * @param method
     *            要排序的实体的属性所对应的get方法
     * @param sort
     *            desc 为正序
     */
    public void Sort(List<E> list, final String method, final String sort) {
        // 用内部类实现排序
        Log.d("chencj", method + sort);
        Collections.sort(list, new Comparator<E>() {

            public int compare(E a, E b) {
                int ret = 0;
                try {
                    // 获取m1的方法名
                    Method m1 = ((E) a).getClass().getMethod(method);
                    // 获取m2的方法名
                    Method m2 = ((E) a).getClass().getMethod(method);
                    LogUtil.d("队列排序："+ method +sort );

                    if (sort != null && "desc".equals(sort)) {
                        LogUtil.d("队列排序开始："+ method +sort );
                        ret = m2.invoke(b, new  Object[]{}).toString()
                                .compareTo(m1.invoke(a, new  Object[]{}).toString());

                    } else {
                        // 正序排序
                        ret = m1.invoke(a, new  Object[]{}).toString()
                                .compareTo(m2.invoke(b, new  Object[]{}).toString());
                    }
                } catch (NoSuchMethodException ne) {
                    LogUtil.e("NoSuchMethodException", ne);
                } catch (IllegalArgumentException e) {
                    LogUtil.e("IllegalArgumentException", e);
                } catch (IllegalAccessException e) {
                    LogUtil.e("IllegalAccessException", e);
                } catch (InvocationTargetException e) {
                    LogUtil.e("InvocationTargetException", e);
                }
                return ret;
            }
        });
    }
}