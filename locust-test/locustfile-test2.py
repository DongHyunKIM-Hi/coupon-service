import random
from locust import task, FastHttpUser


class HelloWorld(FastHttpUser):
  connection_timeout = 10.0
  network_timeout = 10.0

  @task
  def hello(self):
    payload = {
      "userId" : random.randint(1, 10000000),
      "couponId" : 2
    }

    with self.rest("POST", "/v2/issue", json=payload):
      pass