package packets;

public interface MessageAcceptor<T> {
  void acceptMessage(Message message);
  void setAnnotation(T annotation);
  T getAnnotation();
}
