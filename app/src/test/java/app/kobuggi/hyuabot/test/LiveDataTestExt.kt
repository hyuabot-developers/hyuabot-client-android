package app.kobuggi.hyuabot.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> LiveData<T>.valueForTest(): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            removeObserver(this)
        }
    }
    observeForever(observer)
    check(latch.await(2, TimeUnit.SECONDS)) { "LiveData value was never set." }
    @Suppress("UNCHECKED_CAST")
    return data as T
}
