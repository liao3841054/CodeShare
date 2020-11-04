/*
 * Copyright (C) 2004 - 2017 UCWeb Inc. All Rights Reserved.
 * Description : Assert AssertionError 实现
 *
 * Creation    : 2017-04-27
 * Author      : wx107452@alibaba-inc.com
 */
package com.uc.common.util.lang;


/*package*/ class AssertionErrorAssertImpl implements AssertUtil.IAssert {
    @Override
    public void assertDie(String msg) {
        throw new AssertionError(msg);
    }
}
