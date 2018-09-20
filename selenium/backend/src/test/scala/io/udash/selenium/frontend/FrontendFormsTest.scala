package io.udash.selenium.frontend

import com.avsystem.commons._
import io.udash.selenium.SeleniumTest
import org.openqa.selenium.By.{ByClassName, ByCssSelector, ByTagName}

import scala.util.Random

class FrontendFormsTest extends SeleniumTest {
  val url = "/frontend"

  "FrontendForms view" should {
    driver.get(createUrl(url))

    "contain demo elements" in {
      eventually {
        driver.findElementById("checkbox-demo")
        driver.findElementById("check-buttons-demo")
        driver.findElementById("multi-select-demo")
        driver.findElementById("radio-buttons-demo")
        driver.findElementById("select-demo")
        driver.findElementById("text-area-demo")
        driver.findElementById("inputs-demo")
      }
    }

    "contain working checkbox demo" in {
      val checkboxes = driver.findElementById("checkbox-demo")

      def clickAndCheck(propertyName: String, expect: String) = {
        val checkbox = checkboxes.findElement(new ByClassName(s"checkbox-demo-$propertyName"))
        checkbox.click()
        eventually {
          checkboxes.findElements(new ByCssSelector(s"[data-bind=$propertyName]")).asScala
            .foreach { el => assert(el.getText == expect) }
          checkboxes.findElements(new ByClassName(s"checkbox-demo-$propertyName")).asScala
            .foreach { el => assert(el.getAttribute("selected") == checkbox.getAttribute("selected")) }
        }
      }

      for (_ <- 1 to 3) {
        clickAndCheck("a", "false")
        clickAndCheck("b", "true")
        clickAndCheck("c", "No")
        clickAndCheck("a", "true")
        clickAndCheck("b", "false")
        clickAndCheck("c", "Yes")
      }
    }

    "contain working check buttons demo" in {
      val checkButtons = driver.findElementById("check-buttons-demo")

      def clickAndCheck(propertyName: String) = {
        val checkbox = checkButtons.findElement(new ByCssSelector(s"[data-label=$propertyName]")).findElement(new ByTagName("input"))
        checkbox.click()
        eventually {
          checkButtons.findElements(new ByClassName("check-buttons-demo-fruits")).asScala
            .foreach { el =>
              val contains = el.getText.contains(propertyName)
              assert(if (checkbox.getAttribute("selected") != null) contains else !contains)
            }
          checkButtons.findElements(new ByCssSelector(s"[data-label=$propertyName]")).asScala
            .foreach { el =>
              assert(el.findElement(new ByTagName("input")).getAttribute("selected") == checkbox.getAttribute("selected"))
            }
        }
      }

      for (_ <- 1 to 15) {
        clickAndCheck(Random.shuffle(Seq("Apple", "Orange", "Banana")).head)
      }
    }

    "contain working multi select demo" in {
      val multiSelect = driver.findElementById("multi-select-demo")

      def clickAndCheck(propertyName: String, propertyIdx: Int) = {
        val select = multiSelect.findElement(new ByTagName("select"))
        val option = select.findElement(new ByCssSelector(s"[value='$propertyIdx']"))
        option.click()
        eventually {
          multiSelect.findElements(new ByClassName("multi-select-demo-fruits")).asScala
            .foreach { el =>
              val contains = el.getText.contains(propertyName)
              assert(if (option.getAttribute("selected") != null) contains else !contains)
            }
          multiSelect.findElements(new ByTagName("select")).asScala
            .foreach { el =>
              assert(el.findElement(new ByCssSelector(s"[value='$propertyIdx']")).getAttribute("selected") == option.getAttribute("selected"))
            }
        }
      }

      val options = Seq("Apple", "Orange", "Banana").zipWithIndex
      for (_ <- 1 to 15) {
        val (name, idx) = Random.shuffle(options).head
        clickAndCheck(name, idx)
      }
    }

    "contain working radio buttons demo" in {
      val radioButtons = driver.findElementById("radio-buttons-demo")

      def clickAndCheck(propertyName: String, propertyIdx: Int) = {
        val radio = radioButtons.findElement(new ByCssSelector(s"[data-label=$propertyName]")).findElement(new ByTagName("input"))
        radio.click()
        eventually {
          radioButtons.findElements(new ByClassName("radio-buttons-demo-fruits")).asScala
            .foreach { el =>
              assert(el.getText == propertyName)
            }
          radioButtons.findElements(new ByCssSelector(s"input")).asScala
            .foreach { el =>
              val eq = el.getAttribute("selected") == radio.getAttribute("selected")
              assert(if (el.getAttribute("value").toInt == propertyIdx) eq else !eq)
            }
        }
      }

      val options = Seq("Apple", "Orange", "Banana").zipWithIndex
      for (_ <- 1 to 15) {
        val (name, idx) = Random.shuffle(options).head
        clickAndCheck(name, idx)
      }
    }

    "contain working select demo" in {
      val selectDemo = driver.findElementById("select-demo")

      def clickAndCheck(propertyName: String, propertyIdx: Int) = {
        val select = selectDemo.findElement(new ByTagName("select"))
        val option = select.findElement(new ByCssSelector(s"[value='$propertyIdx']"))
        option.click()
        eventually {
          selectDemo.findElements(new ByClassName("select-demo-fruits")).asScala
            .foreach { el =>
              assert(el.getText == propertyName)
            }
          selectDemo.findElements(new ByTagName(s"select")).asScala
            .foreach { el =>
              assert(el.findElement(new ByCssSelector(s"[value='$propertyIdx']")).getAttribute("selected") == option.getAttribute("selected"))
            }
        }
      }

      val options = Seq("Apple", "Orange", "Banana").zipWithIndex
      for (_ <- 1 to 15) {
        val (name, idx) = Random.shuffle(options).head
        clickAndCheck(name, idx)
      }
    }

    "contain working text area demo" in {
      val textAreaDemo = driver.findElementById("text-area-demo")

      def typeAndCheck(text: String) = {
        val textArea = textAreaDemo.findElement(new ByTagName("textarea"))
        textArea.clear()
        textArea.sendKeys(text)
        eventually {
          textAreaDemo.findElements(new ByTagName(s"textarea")).asScala
            .foreach { el =>
              assert(el.getAttribute("value") == text)
            }
        }
      }

      for (_ <- 1 to 15) {
        typeAndCheck(Random.shuffle(Seq("Apple", "Orange", "Banana")).head)
      }
    }

    "contain working text input demo" in {
      val inputsDemo = driver.findElementById("inputs-demo")

      def typeAndCheck(text: String, tpe: String) = {
        val input = inputsDemo.findElement(new ByCssSelector(s"input[type=$tpe]"))
        input.clear()
        input.sendKeys(text)
        eventually {
          inputsDemo.findElements(new ByCssSelector(s"input[type=$tpe]")).asScala
            .foreach { el =>
              assert(el.getAttribute("value") == text)
            }
        }
      }

      for (_ <- 1 to 15) {
        typeAndCheck(Random.shuffle(Seq("Apple", "Orange", "Banana")).head, "text")
        typeAndCheck(Random.shuffle(Seq("Apple", "Orange", "Banana")).head, "password")
        typeAndCheck(Random.shuffle(Seq("123354", "-123", "32")).head, "number")
      }
    }
  }
}
