package com.ponomarev.lettuce.issue.mcve;

import io.lettuce.core.RedisCommandExecutionException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.util.CollectionUtils;
import org.testcontainers.containers.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LettuceIssueTest extends BaseIntegrationTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(LettuceIssueTest.class);

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void test() throws InterruptedException {
		// Arrange
		Duration pause = Duration.ofSeconds(2);
		boolean result = false;
		redisTemplate.execute(new SessionCallbackImpl());
		runClientPause(pause);

		try {
			redisTemplate.execute(new SessionCallbackImpl());
		} catch (QueryTimeoutException e) {
			LOGGER.info(e.getMessage(), e);
			Thread.sleep(pause.toMillis());
			result = true;
		}
		assertTrue(result);

		// Act
		var e = Assertions.assertThrows(RedisSystemException.class,
			() -> redisTemplate.execute(new SessionCallbackImpl()));

		// Assert
		assertNotNull(e);
		assertEquals("Error in execution", e.getMessage());
		assertEquals(RedisCommandExecutionException.class, e.getCause().getClass());
		assertEquals("ERR MULTI calls can not be nested", e.getCause().getMessage());
	}

	private void runClientPause(Duration duration) {
		try {
			Container.ExecResult execResult = container.execInContainer(
				"sh", "-c",
				"redis-cli client pause " + duration.toMillis());
			LOGGER.info(execResult.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private class SessionCallbackImpl implements SessionCallback<Boolean> {
		@Override
		public Boolean execute(RedisOperations operations) throws DataAccessException {
			boolean finished = false;
			try {
				operations.multi();

				List result = operations.exec();
				finished = true;
				if (CollectionUtils.isEmpty(result)) {
					return true;
				}

				return false;
			} finally {
				if (!finished) {
					LOGGER.error("Unexpected error during transaction ");
				}
			}
		}
	}

}
