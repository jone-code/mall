<template>
  <view class="page">
    <view class="form-head">
      <text class="form-tip">请填写真实有效的收货信息</text>
    </view>
    <view class="form">
      <view class="field">
        <text class="label">收件人</text>
        <input v-model="form.receiver" class="input" placeholder="请输入姓名" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input
          v-model="form.phone"
          class="input"
          type="number"
          maxlength="11"
          placeholder="11位手机号"
        />
      </view>
      <view class="field">
        <text class="label">省份</text>
        <input v-model="form.province" class="input" placeholder="如：广东省" />
      </view>
      <view class="field">
        <text class="label">城市</text>
        <input v-model="form.city" class="input" placeholder="如：深圳市" />
      </view>
      <view class="field">
        <text class="label">区/县</text>
        <input v-model="form.district" class="input" placeholder="如：南山区" />
      </view>
      <view class="field field-last">
        <text class="label">详细地址</text>
        <textarea v-model="form.detail" class="textarea" placeholder="街道、门牌号等" />
      </view>
      <label class="default-row">
        <checkbox :checked="form.isDefault" color="#1A1A1A" @click="form.isDefault = !form.isDefault" />
        <text>设为默认地址</text>
      </label>
    </view>

    <view class="footer">
      <button class="save" :loading="saving" @click="onSave">保存</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { reactive, ref } from "vue";
import {
  createAddress,
  listAddresses,
  updateAddress,
} from "@/api/address";

const addressId = ref<number | null>(null);
const saving = ref(false);
const form = reactive({
  receiver: "",
  phone: "",
  province: "",
  city: "",
  district: "",
  detail: "",
  isDefault: false,
});

onLoad(async (query) => {
  if (query?.id) {
    addressId.value = Number(query.id);
    uni.setNavigationBarTitle({ title: "编辑地址" });
    const res = await listAddresses();
    const row = (res.data || []).find((a) => a.id === addressId.value);
    if (row) {
      form.receiver = row.receiver;
      form.phone = row.phone;
      form.province = row.province;
      form.city = row.city;
      form.district = row.district;
      form.detail = row.detail;
      form.isDefault = row.isDefault === 1;
    }
  }
});

async function onSave() {
  const receiver = form.receiver.trim();
  const phone = form.phone.trim();
  const detail = form.detail.trim();
  if (!receiver || !phone || !form.province || !form.city || !form.district || !detail) {
    uni.showToast({ title: "请填写完整", icon: "none" });
    return;
  }
  if (receiver.length < 2) {
    uni.showToast({ title: "收件人至少2个字", icon: "none" });
    return;
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    uni.showToast({ title: "手机号格式不正确", icon: "none" });
    return;
  }
  if (detail.length < 4) {
    uni.showToast({ title: "详细地址至少4个字", icon: "none" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      receiver,
      phone,
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      detail,
      isDefault: form.isDefault,
    };
    if (addressId.value) {
      await updateAddress(addressId.value, payload);
    } else {
      await createAddress(payload);
    }
    uni.showToast({ title: "已保存", icon: "success" });
    setTimeout(() => uni.navigateBack(), 500);
  } catch (e: any) {
    uni.showToast({
      title: e?.payload?.message || e?.message || "保存失败",
      icon: "none",
    });
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped lang="scss">

.page {
  @include page-shell;
  padding: $spacing-page;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.form-head {
  margin-bottom: 16rpx;
}

.form-tip {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.form {
  @include form-card;
}

.field {
  @include form-field;
}

.field-last {
  border-bottom: none;
}

.label {
  display: block;
  font-size: 26rpx;
  color: $color-text-secondary;
  margin-bottom: 12rpx;
}

.input,
.textarea {
  width: 100%;
  font-size: 28rpx;
  color: $color-text-primary;
}

.textarea {
  min-height: 120rpx;
}

.default-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 0 16rpx;
  font-size: 28rpx;
  color: $color-text-primary;
}

.footer {
  @include fixed-footer;
}

.save {
  flex: 1;
  @include btn-primary;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  margin: 0;
}
</style>
