
package com.test.ad.demo.customize;

import com.adgain.sdk.api.AdAppInfo;
import com.anythink.core.api.ATAdAppInfo;

public class AdGainDownloadAppInfo extends ATAdAppInfo {
    public String publisher;
    public String appVersion;
    public String appPrivacyLink;
    public String appPermissionLink;
    public String appName;
    public String packageName;
    public long appSize;
    public String appDownloadCount;

    public AdGainDownloadAppInfo(AdAppInfo info, String downloadCount) {
        publisher = info.getAuthorName();
        appVersion = info.getVersionName();
        appPrivacyLink = info.getPrivacyUrl();
        appPermissionLink = info.getPermissionsUrl();
        appName = info.getAppName();
        packageName = info.getPackageName();
        appSize = info.getAppSize();

        appDownloadCount = downloadCount;
    }

    @Override
    public String getPublisher() {
        return publisher;
    }

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public String getAppPrivacyUrl() {
        return appPrivacyLink;
    }

    @Override
    public String getAppPermissonUrl() {
        return appPermissionLink;
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getAppPackageName() {
        return packageName;
    }

    @Override
    public String getDownloadCount() {
        return appDownloadCount;
    }

    @Override
    public long getAppSize() {
        return appSize;
    }
}
