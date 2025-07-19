package dev.handsup.bidding.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
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
import dev.handsup.notification.service.FcmService;
import dev.handsup.support.TestContainerSupport;
import dev.handsup.user.domain.User;
import dev.handsup.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
@Import({TestAuditingConfig.class, FcmConfig.class})
class BiddingNotificationTest extends TestContainerSupport {

	private Auction auction;
	private User user;
	private int threadCount, poolSize;

	@Autowired
	private FcmService fcmService; // FCM 호출만 Moc
	@Autowired
	private BiddingNotificationService biddingService;

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
		user = userRepository.save(UserFixture.user1());
		// fcmService.saveFcmToken(user.getId(), "내토큰");
		threadCount = 20;
		poolSize = 32;

	}

	@AfterEach
	void clean(){
		biddingRepository.deleteAll();
	}

	@DisplayName("20개 입찰 동시 생성-> 알림 동기 호출")
	@Test
	void bidding_sync_notification() throws InterruptedException {
		RegisterBiddingRequest[] requests = new RegisterBiddingRequest[threadCount];
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch(
			String.format("하나의 트랜잭션에서 알림 외부 API까지 호출\n(스레드 %d, 스레드 풀 %d개)", threadCount, poolSize)
		);
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			final int idx = i;
			requests[idx] = RegisterBiddingRequest.from(auction.getInitPrice() + 1000 * (idx + 1));
			executorService.submit(() -> {
				try {
					biddingService.registerBiddingSync(requests[idx], auction.getId(), user);
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
		System.out.println(stopWatch.prettyPrint());
		System.out.println("카운트"+biddingRepository.countByAuctionId(auction.getId()));
	}

	@DisplayName("20개 입찰 동시에 생성 -> 알림 비동기 호출")
	@Test
	void bidding_async_notification() throws InterruptedException {
		RegisterBiddingRequest[] requests = new RegisterBiddingRequest[threadCount];
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch(
			String.format("알림 호출 이벤트 리스너로 처리\n(스레드 %d, 스레드 풀 %d개)", threadCount, poolSize)
		);
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			final int idx = i;
			requests[idx] = RegisterBiddingRequest.from(auction.getInitPrice() + 1000 * (idx + 1));
			executorService.submit(() -> {
				try {
					biddingService.registerBiddingWithAsync(requests[idx], auction.getId(), user);
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
		System.out.println(stopWatch.prettyPrint());
		System.out.println("카운트"+biddingRepository.countByAuctionId(auction.getId()));
	}
}
