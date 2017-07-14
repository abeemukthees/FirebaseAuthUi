/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package msa.auth.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleRes;

import java.util.Collections;
import java.util.List;

import msa.auth.AuthUI.IdpConfig;
import msa.auth.util.Preconditions;

/**
 * Encapsulates the core parameters and data captured during the authentication flow, in
 * a serializable manner, in order to pass data between activities.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class FlowParameters implements Parcelable {
    public static final Creator<FlowParameters> CREATOR = new Creator<FlowParameters>() {
        @Override
        public FlowParameters createFromParcel(Parcel in) {
            String appName = in.readString();
            List<IdpConfig> providerInfo = in.createTypedArrayList(IdpConfig.CREATOR);
            int themeId = in.readInt();
            int logoId = in.readInt();
            int backgroundId = in.readInt();
            int phoneButtonBackgroundColor = in.readInt();

            String termsOfServiceUrl = in.readString();
            String privacyPolicyUrl = in.readString();
            boolean enableCredentials = in.readInt() != 0;
            boolean enableHints = in.readInt() != 0;
            boolean allowNewEmailAccounts = in.readInt() != 0;
            boolean alwaysShowAuthMethodPicker = false;

            return new FlowParameters(
                    appName,
                    providerInfo,
                    themeId,
                    logoId,
                    backgroundId,
                    phoneButtonBackgroundColor,
                    termsOfServiceUrl,
                    privacyPolicyUrl,
                    enableCredentials,
                    enableHints,
                    allowNewEmailAccounts,
                    alwaysShowAuthMethodPicker);
        }

        @Override
        public FlowParameters[] newArray(int size) {
            return new FlowParameters[size];
        }
    };
    @NonNull
    public final String appName;
    @NonNull
    public final List<IdpConfig> providerInfo;
    @StyleRes
    public final int themeId;
    @DrawableRes
    public final int logoId;
    @DrawableRes
    public final int backgroundId;
    @ColorRes
    public final int phoneButtonBackgroundColor;
    @Nullable
    public final String termsOfServiceUrl;
    @Nullable
    public final String privacyPolicyUrl;
    public final boolean allowNewEmailAccounts;
    public final boolean enableCredentials;
    public final boolean enableHints;
    public final boolean alwaysShowAuthMethodPicker;

    public FlowParameters(
            @NonNull String appName,
            @NonNull List<IdpConfig> providerInfo,
            @StyleRes int themeId,
            @DrawableRes int logoId,
            @DrawableRes int backgroundId,
            @ColorRes int phoneButtonBackgroundColor,
            @Nullable String termsOfServiceUrl,
            @Nullable String privacyPolicyUrl,
            boolean enableCredentials,
            boolean enableHints,
            boolean allowNewEmailAccounts,
            boolean alwaysShowAuthMethodPicker) {
        this.appName = Preconditions.checkNotNull(appName, "appName cannot be null");
        this.providerInfo = Collections.unmodifiableList(
                Preconditions.checkNotNull(providerInfo, "providerInfo cannot be null"));
        this.themeId = themeId;
        this.logoId = logoId;
        this.backgroundId = backgroundId;
        this.phoneButtonBackgroundColor = phoneButtonBackgroundColor;
        this.termsOfServiceUrl = termsOfServiceUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.enableCredentials = enableCredentials;
        this.enableHints = enableHints;
        this.allowNewEmailAccounts = allowNewEmailAccounts;
        this.alwaysShowAuthMethodPicker = alwaysShowAuthMethodPicker;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeTypedList(providerInfo);
        dest.writeInt(themeId);
        dest.writeInt(logoId);
        dest.writeInt(backgroundId);
        dest.writeInt(phoneButtonBackgroundColor);
        dest.writeString(termsOfServiceUrl);
        dest.writeString(privacyPolicyUrl);
        dest.writeInt(enableCredentials ? 1 : 0);
        dest.writeInt(enableHints ? 1 : 0);
        dest.writeInt(allowNewEmailAccounts ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
