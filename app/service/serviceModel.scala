package service

import play.api.libs.json.{Format, Json}

object serviceModel {

  case class Message(role: String, content: String)
  case class Choice(index: Long, message: Message, finish_reason: String)
  case class Usage(prompt_tokens: Long, completion_tokens: Long, total_tokens: Long)

  case class ChatGptApiResponse(
                          id: String,
                          created: Long,
                          model: String,
                          choices: List[Choice],
                          usage: Usage
                        )


  object Message {
    implicit val format: Format[Message] = Json.format[Message]
  }

  object Choice {
    implicit val format: Format[Choice] = Json.format[Choice]
  }

  object Usage {
    implicit val format: Format[Usage] = Json.format[Usage]
  }

  object ChatGptApiResponse {
    implicit val format: Format[ChatGptApiResponse] = Json.format[ChatGptApiResponse]
  }
}
