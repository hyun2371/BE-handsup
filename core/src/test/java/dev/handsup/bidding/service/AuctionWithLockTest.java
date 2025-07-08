package dev.handsup.bidding.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StopWatch;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.domain.product.product_category.ProductCategory;
import dev.handsup.auction.repository.auction.AuctionRepository;
import dev.handsup.auction.repository.product.ProductCategoryRepository;
import dev.handsup.bidding.dto.request.RegisterBiddingRequest;
import dev.handsup.bidding.repository.BiddingRepository;
import dev.handsup.common.config.FcmConfig;
import dev.handsup.config.TestAuditingConfig;
import dev.handsup.fixture.AuctionFixture;
import dev.handsup.fixture.UserFixture;
import dev.handsup.support.TestContainerSupport;
import dev.handsup.user.domain.User;
import dev.handsup.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
@Import({TestAuditingConfig.class, FcmConfig.class})
public class AuctionWithLockTest extends TestContainerSupport {
	private Auction auction;
	private User user;
	private RegisterBiddingRequest request;
	private int threadCount, poolSize;

	@Autowired
	private BiddingService biddingService;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BiddingRepository biddingRepository;

	@BeforeEach
	void setUp() {
		ProductCategory productCategory = productCategoryRepository.save(ProductCategory.from("디지털 기기"));
		auction = auctionRepository.save(AuctionFixture.auction(productCategory));
		request = RegisterBiddingRequest.from(auction.getInitPrice() + 1000);

		user = userRepository.save(UserFixture.user1());
		threadCount = 100;
		poolSize = 32;
	}

	@DisplayName("[동시 요청 시, 입찰 금액이 모두 같다면 하나의 입찰만 저장된다.]")
	@Test
	void distributed_lock_test() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					biddingService.registerBidding(request, auction.getId(), user);
				} catch (Exception e) {
					log.info("{concurrency test error = {}", e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		stopWatch.stop();
		log.info("동시성 테스트 소요 시간(ms): {}", stopWatch.getTotalTimeMillis());

		Assertions.assertThat(biddingRepository.findAll()).hasSize(1);
	}

	@DisplayName("[동시 요청 시, 입찰 금액이 모두 같다면 하나의 입찰만 저장된다.]")
	@Test
	void concurrency_test() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					biddingService.registerBidding(request, auction.getId(), user);
				} catch (Exception e) {
					log.info("{concurrency test error = {}", e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		stopWatch.stop();
		log.info("동시성 테스트 소요 시간(ms): {}", stopWatch.getTotalTimeMillis());

		Assertions.assertThat(biddingRepository.findAll()).hasSize(1);
	}
}
