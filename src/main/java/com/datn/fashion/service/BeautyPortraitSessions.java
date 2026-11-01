package com.datn.fashion.service;

import com.datn.fashion.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.DisposableBean;
import java.util.*;
import java.util.concurrent.*;

/** Explicit short-lived portrait session: RAM only, owner-bound, bounded, never logged. */
@Component
public class BeautyPortraitSessions implements DisposableBean {
    private static final long TTL_MS=180_000;
    private record Entry(String owner,ImageUploadValidator.ValidatedImage image,long expires) {
        @Override public String toString(){return "PortraitSession[REDACTED]";}
    }
    private final Map<String,Entry> cache=new HashMap<>();
    private final ScheduledExecutorService cleanup=Executors.newSingleThreadScheduledExecutor(r->{var t=new Thread(r,"nova-portrait-expiry");t.setDaemon(true);return t;});
    public BeautyPortraitSessions(){cleanup.scheduleAtFixedRate(this::expire,10,10,TimeUnit.SECONDS);}
    private void wipe(Entry e){Arrays.fill(e.image().bytes(),(byte)0);}
    synchronized void expire(){long now=System.currentTimeMillis();cache.values().removeIf(e->{if(e.expires()<=now){wipe(e);return true;}return false;});}
    public synchronized String put(String owner,ImageUploadValidator.ValidatedImage image){
        expire();cache.values().removeIf(e->{if(e.owner().equals(owner)){wipe(e);return true;}return false;});
        if(cache.size()>=4)throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"STUDIO_BUSY","Studio đang bận. Vui lòng thử lại sau.");
        String token=UUID.randomUUID().toString();
        cache.put(token,new Entry(owner,new ImageUploadValidator.ValidatedImage(image.bytes().clone(),image.filename()),System.currentTimeMillis()+TTL_MS));return token;
    }
    public synchronized ImageUploadValidator.ValidatedImage get(String owner,String token){
        expire();Entry e=cache.get(token);
        if(e==null||!e.owner().equals(owner))throw new ApiException(HttpStatus.GONE,"PORTRAIT_EXPIRED","Phiên ảnh đã hết hạn. Bấm thử để kiểm tra và gửi lại ảnh đang chọn.");
        return new ImageUploadValidator.ValidatedImage(e.image().bytes().clone(),e.image().filename());
    }
    public synchronized void remove(String owner,String token){Entry e=cache.get(token);if(e!=null&&e.owner().equals(owner)){cache.remove(token);wipe(e);}}
    @Override public synchronized void destroy(){cleanup.shutdownNow();cache.values().forEach(this::wipe);cache.clear();}
}
