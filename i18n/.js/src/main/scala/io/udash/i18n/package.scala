package io.udash

import io.udash.bindings.modifiers.Binding
import io.udash.i18n.bindings._
import org.scalajs.dom.Element
import scalatags.JsDom.Modifier

import scala.concurrent.Future

package object i18n {
  type LangProperty = ReadableProperty[Lang]

  implicit def langFromProperty(implicit property: LangProperty): Lang =
    property.get

  /**
   * Binds translated string in DOM element.
   * @param translation Future containing translated string or error.
   * @param placeholder Placeholder, if `None` passed it will be empty text node.
   * @param rawHtml Flag that force to use this translation as raw HTML, disabled by default
   */
  def translated(
    translation: Future[Translated], placeholder: Option[Element] = None, rawHtml: Boolean = false
  ): Modifier =
    new TranslationBinding(translation, placeholder, rawHtml)

  /**
   * Binds translated string in DOM element and updates it when application language changes.
   * @param key TranslationKey which will be used in order to get text.
   * @param translator Should apply any needed arguments to TranslationKey and create `Future[Translated]`.
   * @param placeholder Placeholder, if `None` passed it will be empty text node.
   * @param rawHtml Flag that force to use this translation as raw HTML, disabled by default
   */
  def translatedDynamic[Key <: TranslationKey](
    key: Key, placeholder: Option[Element] = None, rawHtml: Boolean = false
  )(
    translator: Key => Future[Translated]
  )(implicit lang: LangProperty): Binding =
    new DynamicTranslationBinding(key, translator, placeholder, rawHtml)

  /**
    * Binds translated string in DOM element attribute.
    * @param translation Future containing translated string or error.
    * @param attr Attribute name which gonna be updated when `translation` text become ready.
    */
  def translatedAttr(translation: Future[Translated], attr: String): Modifier =
    new AttrTranslationBinding(translation, attr)

  /**
    * Binds translated string in DOM element attribute and updates it when application language changes.
    * @param key TranslationKey which will be used in order to get text.
    * @param translator Should apply any needed arguments to TranslationKey and create `Future[Translated]`.
    * @param attr Attribute name which gonna be updated when `translation` text become ready.
    */
  def translatedAttrDynamic[Key <: TranslationKey](
    key: Key, attr: String
  )(translator: Key => Future[Translated])(implicit lang: LangProperty): Binding =
    new DynamicAttrTranslationBinding(key, translator, attr)
}
