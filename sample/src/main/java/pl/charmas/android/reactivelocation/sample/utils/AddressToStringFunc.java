package pl.charmas.android.reactivelocation.sample.utils;

import android.location.Address;

import io.reactivex.functions.Function;

public class AddressToStringFunc implements Function<Address, String> {
    @Override
    public String apply(Address address) {
        if (address == null) return "";

        String addressLines = "";
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressLines += address.getAddressLine(i) + '\n';
        }
        return addressLines;
    }
}
