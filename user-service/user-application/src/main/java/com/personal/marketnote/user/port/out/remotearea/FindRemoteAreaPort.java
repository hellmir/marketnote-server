package com.personal.marketnote.user.port.out.remotearea;

public interface FindRemoteAreaPort {
    boolean existsByAddress(String province, String district, String village, String subarea);
}
