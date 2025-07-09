package dev.handsup.bidding.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

	@Autowired
	private OptimisticLockAuctionFacade optimisticLockAuctionFacade;

	@BeforeEach
	void setUp() {
		ProductCategory productCategory = productCategoryRepository.save(ProductCategory.from("디지털 기기"));
		auction = auctionRepository.save(AuctionFixture.auction(productCategory));
		user = userRepository.save(UserFixture.user1());
		threadCount = 2000;
		poolSize = 32;
	}

	@AfterEach
	void clean(){
		biddingRepository.deleteAll();
	}

	@Disabled
	@DisplayName("[동시 요청 시, 입찰 금액이 모두 같다면 하나의 입찰만 저장된다.]")
	@Test
	void distributed_lock_same_bidding_price() throws InterruptedException {
		RegisterBiddingRequest request = RegisterBiddingRequest.from(auction.getInitPrice() + 1000);
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch(
			String.format("경매 분산락 기반 입찰 중복 방지 테스트\n(스레드 %d, 스레드 풀 %d개)", threadCount, poolSize)
		);
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					biddingService.registerBiddingWithDistributedLock(request, auction.getId(), user);
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

		Assertions.assertThat(biddingRepository.findAll()).hasSize(1);
	}

	@DisplayName("[동시 요청 시, 입찰 금액이 모두 같다면 하나의 입찰만 저장된다.]")
	@Test
	void pessimistic_lock_same_bidding_price() throws InterruptedException {
		RegisterBiddingRequest request = RegisterBiddingRequest.from(auction.getInitPrice() + 1000);
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch(
			String.format("경매 비관락 기반 입찰 중복 방지 테스트\n(스레드 %d, 스레드 풀 %d개)", threadCount, poolSize)
		);
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					biddingService.registerBiddingWithPessimisticLock(request, auction.getId(), user);
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

		Assertions.assertThat(biddingRepository.findAll()).hasSize(1);
	}

	@DisplayName("[동시 요청 시, 입찰 금액이 모두 같다면 하나의 입찰만 저장된다.]")
	@Test
	void optimistic_lock_same_bidding_price() throws InterruptedException {
		RegisterBiddingRequest request = RegisterBiddingRequest.from(auction.getInitPrice() + 1000);
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		StopWatch stopWatch = new StopWatch(
			String.format("경매 낙관락 기반 입찰 중복 방지 테스트\n(스레드 %d, 스레드 풀 %d개)", threadCount, poolSize)
		);
		stopWatch.start();
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					optimisticLockAuctionFacade.registerBidding(request, auction.getId(), user);
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

		Assertions.assertThat(biddingRepository.findAll()).hasSize(1);
	}

	@DisplayName("[여러 금액 동시 입찰 시, 최고가 갱신 입찰만 저장된다.]")
	@Test
	void optimistic_lock_diff_bidding_price() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		Random random = new Random();
		StopWatch stopWatch = new StopWatch("Concurrent Optimistic Bidding Test");
		stopWatch.start();

		for (int i = 0; i < threadCount; i++) {
			int rand = 1 + random.nextInt(5); // 대략 20% 정도 입찰 금액 겹치도록 설정
			final int biddingPrice = auction.getInitPrice() + rand * 1000;
			executor.submit(() -> {
				try {
					optimisticLockAuctionFacade.registerBidding(
						RegisterBiddingRequest.from(biddingPrice),
						auction.getId(),
						user
					);
					successCount.incrementAndGet();
				} catch (Exception e) {
					log.info("{concurrency test error = {}", e.getMessage());
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		stopWatch.stop();
		System.out.println(stopWatch.prettyPrint());

		assertThat(biddingRepository.countByAuctionId(auction.getId()))
			.isEqualTo(successCount.get());
		assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
	}

	@DisplayName("[여러 금액 동시 입찰 시, 최고가 갱신 입찰만 저장된다.]")
	@Test
	void pessimistic_lock_diff_bidding_price() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		CountDownLatch latch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		Random random = new Random();

		StopWatch stopWatch = new StopWatch("Concurrent Pessimistic Bidding Test");
		stopWatch.start();

		for (int i = 0; i < threadCount; i++) {
			int rand = 1 + random.nextInt(5); // 대략 20% 정도 입찰 금액 겹치도록 설정
			final int biddingPrice = auction.getInitPrice() + rand * 1000;
			executor.submit(() -> {
				try {
					biddingService.registerBiddingWithPessimisticLock(
						RegisterBiddingRequest.from(biddingPrice),
						auction.getId(),
						user
					);
					successCount.incrementAndGet();
				} catch (Exception e) {
					log.info("{concurrency test error = {}", e.getMessage());
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		stopWatch.stop();
		System.out.println(stopWatch.prettyPrint());

		assertThat(biddingRepository.countByAuctionId(auction.getId()))
			.isEqualTo(successCount.get());
		assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
	}
}
