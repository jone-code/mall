import { http } from "@/utils/request";

export interface AddressItem {
  id: number;
  receiver: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  regionCode?: string;
  isDefault?: number;
  fullAddress?: string;
}

export interface AddressPayload {
  receiver: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  regionCode?: string;
  isDefault?: boolean;
}

export function listAddresses() {
  return http.get<AddressItem[]>("/me/addresses");
}

export function createAddress(payload: AddressPayload) {
  return http.post<{ id: number }>("/me/addresses", payload);
}

export function updateAddress(id: number, payload: Partial<AddressPayload>) {
  return http.put<void>(`/me/addresses/${id}`, payload);
}

export function setDefaultAddress(id: number) {
  return http.put<void>(`/me/addresses/${id}/default`);
}

export function deleteAddress(id: number) {
  return http.del<void>(`/me/addresses/${id}`);
}
