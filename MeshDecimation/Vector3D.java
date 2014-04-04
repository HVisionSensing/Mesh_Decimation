
public class Vector3D {

	int ID;

	float X;
	float Y;
	float Z;

	private float magnitude;// also known as length
	private float squareMagnitude;

	public Vector3D() {
		X = 0f;
		Y = 0f;
		Z = 0f;
	}

	public Vector3D(float x, float y, float z) {
		X = x;
		Y = y;
		Z = z;
	}

	public void setX(float x) {
		X = x;
	}

	public void setY(float y) {
		Y = y;
	}

	public void setZ(float z) {
		Z = z;
	}

	public void setTo(Vector3D v) {
		X = v.X;
		Y = v.Y;
		Z = v.Z;
	}

	public void computeSquareMagnitude() {
		squareMagnitude = X * X + Y * Y + Z * Z;
	}

	public void computeMagnitude() {
		magnitude = (float) Math.sqrt(squareMagnitude);
	}

	public float getMagnitude() {
		return magnitude;
	}

	public float getSquareMagnitude() {
		return squareMagnitude;
	}

	public void normalize() {
		X = X / magnitude;
		Y = Y / magnitude;
		Z = Z / magnitude;
	}

	public void scale(float value) {
		X = X * value;
		Y = Y * value;
		Z = Z * value;
	}

	public void scale(int value) {
		X = X * value;
		Y = Y * value;
		Z = Z * value;
	}

	public void scale(double value) {
		X = (float) (X * value);
		Y = (float) (Y * value);
		Z = (float) (Z * value);
	}

	public void add(Vector3D vector) {
		X = X + vector.X;
		Y = Y + vector.Y;
		Z = Z + vector.Z;
	}

	public void subtract(Vector3D vector) {
		X = X + vector.X;
		Y = Y + vector.Y;
		Z = Z + vector.Z;
	}

	public Vector3D componentProduct(Vector3D vector) {
		return new Vector3D(X * vector.X, Y * vector.Y, Z * vector.Z);
	}

	public void componentProductUpdate(Vector3D vector) {
		X = X * vector.X;
		Y = Y * vector.Y;
		Z = Z * vector.Z;
	}

	public float dotProduct(Vector3D vector) {
		return (X * vector.X + Y * vector.Y + Z * vector.Z);
	}

	public Vector3D crossProduct(Vector3D vector) {
		return new Vector3D((Y * vector.Z - Z * vector.Y), (Z * vector.X - X
				* vector.Z), (X * vector.Y - Y * vector.X));
	}

	public void crossProductUpdate(Vector3D vector) {
		float x = Y * vector.Z - Z * vector.Y;
		float y = Z * vector.X - X * vector.Z;
		float z = X * vector.Y - Y * vector.X;
		X = x;
		Y = y;
		Z = z;
	}

	public void makeOrthonormalBasis(Vector3D a, Vector3D b, Vector3D c) {
		a.normalize();
		c = a.crossProduct(b);
		if (c.getSquareMagnitude() == 0) {
			return;
		}
		c.normalize();
		b = c.crossProduct(a);
	}

	
	public void rotateX(float cosAngle, float sinAngle) {
		float newY = Y * cosAngle - Z * sinAngle;
		float newZ = Y * sinAngle + Z * cosAngle;
		Y = newY;
		Z = newZ;
	}
	
	public void rotateY(float cosAngle, float sinAngle) {
		float newX = Z * sinAngle + X * cosAngle;
		float newZ = Z * cosAngle - X * sinAngle;
		X = newX;
		Z = newZ;
	}

	public void rotateZ(float cosAngle, float sinAngle) {
		float newX = X * cosAngle - Y * sinAngle;
		float newY = X * sinAngle + Y * cosAngle;
		X = newX;
		Y = newY;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Vector3D))
			return false;

		Vector3D temp = (Vector3D) obj;
		return (temp.X == X && temp.Y == Y && temp.Z == Z);
	}

	@Override
	public String toString() {
		return ("This vector is: (" + X + ", " + Y + ", " + Z + ")");
	}

}
