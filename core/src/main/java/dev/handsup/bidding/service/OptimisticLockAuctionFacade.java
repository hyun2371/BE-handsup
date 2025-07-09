package dev.handsup.bidding.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import dev.handsup.bidding.dto.request.RegisterBiddingRequest;
import dev.handsup.bidding.dto.response.BiddingResponse;
import dev.handsup.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockAuctionFacade {
	private final BiddingService biddingService;
	private static final long RETRY_DELAY_MS = 50;

	public BiddingResponse registerBidding(
		RegisterBiddingRequest request, Long auctionId, User bidder
	) throws InterruptedException {
		int retryCount = 0;
		while (true) {
			try {
				BiddingResponse response = biddingService.registerBiddingWithOptimisticLock(request, auctionId,
					bidder);
				log.info("경매 입찰 성공 - auctionId: {}, bidderId: {}, 총 시도횟수: {}",
					auctionId, bidder.getId(), retryCount++);
				return response;
			} catch (ObjectOptimisticLockingFailureException e) {
				{
					log.info("경매 입찰 실패 - auctionId: {}, bidderId: {}, 총 시도횟수: {}",
						auctionId, bidder.getId(), retryCount);
					Thread.sleep(RETRY_DELAY_MS);
				}
			}
		}
	}
}
