package com.datn.fashion.dto.beauty;

import java.util.List;
import java.util.Set;
public record BeautyTryOnResponse(String status,String imageBase64,String originalBase64,String engineVersion,double processingTimeMs,List<String> warnings,String portraitToken,List<Double> faceBounds) {
    public BeautyTryOnResponse(String status,String imageBase64,String originalBase64,String engineVersion,double processingTimeMs,List<String> warnings){this(status,imageBase64,originalBase64,engineVersion,processingTimeMs,warnings,null,null);}
    public BeautyTryOnResponse withToken(String token){return new BeautyTryOnResponse(status,imageBase64,originalBase64,engineVersion,processingTimeMs,warnings,token,faceBounds);}
    @Override public String toString(){return "BeautyTryOnResponse[status="+status+", images=REDACTED, engineVersion="+engineVersion+"]";}
    public boolean valid() {
        if(!Set.of("SUCCESS","NO_FACE","MULTIPLE_FACES","LOW_QUALITY","INVALID_IMAGE","ERROR").contains(status==null?"":status)
            ||engineVersion==null||engineVersion.isBlank()||!Double.isFinite(processingTimeMs)||processingTimeMs<0||warnings==null)return false;
        if(!"SUCCESS".equals(status))return imageBase64==null&&originalBase64==null;
        return png(imageBase64)&&png(originalBase64)&&(faceBounds==null||(faceBounds.size()==4&&faceBounds.stream().allMatch(v->v!=null&&Double.isFinite(v)&&v>=0&&v<=1)&&faceBounds.get(0)<faceBounds.get(2)&&faceBounds.get(1)<faceBounds.get(3)));
    }
    private boolean png(String value) {
        if(value==null||value.length()>12_000_000||!value.startsWith("iVBORw0KGgo"))return false;
        try{return java.util.Base64.getDecoder().decode(value).length>40;}catch(IllegalArgumentException e){return false;}
    }
}
