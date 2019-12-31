package com.navelplace.collections;


import static org.assertj.core.api.Assertions.assertThat;

import com.navelplace.spring.cache.CacheStrategy;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;

public class CollectionUtilsJavaTest {
  @Test
  public void testStatics() {
    Collection<Integer> list = Collections.singletonList(1);
    assertThat(CollectionUtils.containsExactlyInAnyOrder(list, list)).isTrue();
    assertThat(CollectionUtils.containsExactlyInAnyOrder(list.toArray(), list.toArray())).isTrue();
    assertThat(CollectionUtils.containsExactlyInAnyOrder(list, list.toArray())).isTrue();
    assertThat(CollectionUtils.containsExactlyInAnyOrder(list.toArray(), list)).isTrue();
  }
}
