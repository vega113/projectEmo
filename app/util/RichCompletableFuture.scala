// In a file named RichCompletableFuture.scala
package util

import java.util.concurrent.CompletableFuture
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

object RichCompletableFuture {
  implicit class RichCF[T](javaFuture: CompletableFuture[T]) {
    def asScala: Future[T] = {
      val promise = Promise[T]()
      javaFuture.whenComplete { (result: T, exception: Throwable) =>
        if (exception == null) promise.success(result)
        else promise.failure(exception)
      }
      promise.future
    }
  }
}