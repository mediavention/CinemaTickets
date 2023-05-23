package thirdparty.discount;

import thirdparty.discount.exception.InvalidDiscountCodeException;

public interface DiscountService {

    Discount getDiscountPercentage(long accountId, String discountCode) throws InvalidDiscountCodeException;
}
