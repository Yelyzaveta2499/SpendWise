// ...existing code...
  return (
    <Animated.View style={[styles.container, { opacity: overlayOpacity }]} pointerEvents="none">
      {/* Blue background layer (visible only before expand) */}
      <Animated.View
        style={[
          StyleSheet.absoluteFill,
          { backgroundColor: '#1565C0', opacity: bgOpacity },
        ]}
      />

      {/* Centered hollow card outline (transparent inside) */}
      <View style={styles.center}>
        <Animated.View
          style={[
            styles.cardOutline,
            { transform: [{ scale }] },
          ]}
        />
      </View>
    </Animated.View>
  );
// ...existing code...
const styles = StyleSheet.create({
  // ...existing code...
  logo: {
    // removed image sizing; replaced by cardOutline
    width: 0,
    height: 0,
  },
  cardOutline: {
    width: Platform.select({ ios: 200, android: 200 }),
    height: Platform.select({ ios: 140, android: 140 }),
    borderWidth: 2,
    borderColor: 'rgba(255,255,255,0.9)',
    borderRadius: 12, // approximates rx=24 at smaller base size
    backgroundColor: 'transparent', // transparent center
    shadowColor: '#000',
    shadowOpacity: 0.35,
    shadowRadius: 12,
    elevation: 8,
  },
});
