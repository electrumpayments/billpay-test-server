package com.electrum.billpaytestserver.handler;

import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicRequest;
import io.electrum.vas.model.BasicResponse;
import io.electrum.vas.model.BasicReversal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * T is request type U is response type
 */
public abstract class BaseDualRequestHandler<W extends BasicRequest, X extends BasicResponse, Y extends BasicAdvice, Z extends BasicReversal>
      extends BaseRequestHandler<W, X> {
   private static final Logger log = LoggerFactory.getLogger(BaseDualRequestHandler.class);

   // handleConfirm

   // handleReversal

   // abstract getConfirmResponse

   // abstract getReversalResponse
}
